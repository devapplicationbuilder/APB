package org.quickdev.api.application;


import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickdev.api.application.ApplicationEndpoints.CreateApplicationRequest;
import org.quickdev.api.application.view.ApplicationView;
import org.quickdev.api.common.mockuser.WithMockUser;
import org.quickdev.api.datasource.DatasourceApiService;
import org.quickdev.api.datasource.DatasourceApiServiceIntegrationTest;
import org.quickdev.api.permission.view.CommonPermissionView;
import org.quickdev.api.permission.view.PermissionItemView;
import org.quickdev.domain.application.model.Application;
import org.quickdev.domain.application.model.ApplicationType;
import org.quickdev.domain.datasource.model.Datasource;
import org.quickdev.domain.permission.model.ResourceRole;
import org.quickdev.sdk.exception.BizError;
import org.quickdev.sdk.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SuppressWarnings({"OptionalGetWithoutIsPresent"})
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j(topic = "ApplicationApiServiceIntegrationTest")
public class ApplicationApiServiceIntegrationTest {

    @Autowired
    private ApplicationApiService applicationApiService;
    @Autowired
    private DatasourceApiService datasourceApiService;

    @SuppressWarnings("ConstantConditions")
    @Test
    @WithMockUser(id = "user02")
    public void testCreateApplicationSuccess() {

        Mono<Datasource> datasourceMono = datasourceApiService.create(DatasourceApiServiceIntegrationTest.buildMysqlDatasource("mysql07")).cache();
        Mono<CommonPermissionView> commonPermissionViewMono =
                datasourceMono.flatMap(datasource -> datasourceApiService.getPermissions(datasource.getId()));
        Mono<Boolean> deleteMono = commonPermissionViewMono.flatMap(commonPermissionView -> {
            String permissionId = commonPermissionView.getUserPermissions().stream()
                    .filter(permissionItemView -> permissionItemView.getId().equals("user02"))
                    .findFirst()
                    .map(PermissionItemView::getPermissionId)
                    .get();
            return datasourceApiService.updatePermission(permissionId, ResourceRole.VIEWER);
        });
        //
        Mono<ApplicationView> applicationViewMono = datasourceMono.map(datasource -> new CreateApplicationRequest(
                        "org01",
                        "app05",
                        ApplicationType.APPLICATION.getValue(),
                        Map.of("comp", "table"),
                        Map.of("comp", "list", "queries", Set.of(Map.of("datasourceId", datasource.getId()))),
                        null))
                .delayUntil(__ -> deleteMono)
                .flatMap(createApplicationRequest -> applicationApiService.create(createApplicationRequest));

        StepVerifier.create(applicationViewMono)
                .assertNext(applicationView -> Assert.assertNotNull(applicationView.getApplicationInfoView().getApplicationId()))
                .verifyComplete();
    }

    @Ignore
    @SuppressWarnings("ConstantConditions")
    @Test
    @WithMockUser(id = "user02")
    public void testUpdateApplicationFailedDueToLackOfDatasourcePermissions() {

        Mono<Datasource> datasourceMono = datasourceApiService.create(DatasourceApiServiceIntegrationTest.buildMysqlDatasource("mysql08")).cache();
        Mono<CommonPermissionView> commonPermissionViewMono =
                datasourceMono.flatMap(datasource -> datasourceApiService.getPermissions(datasource.getId()));
        Mono<Boolean> deleteMono = commonPermissionViewMono.flatMap(commonPermissionView -> {
            String permissionId = commonPermissionView.getUserPermissions().stream()
                    .filter(permissionItemView -> permissionItemView.getId().equals("user02"))
                    .findFirst()
                    .map(PermissionItemView::getPermissionId)
                    .get();
            return datasourceApiService.deletePermission(permissionId);
        });
        //
        Mono<ApplicationView> applicationViewMono = datasourceMono.map(datasource -> new CreateApplicationRequest(
                        "org01",
                        "app03",
                        ApplicationType.APPLICATION.getValue(),
                        Map.of("comp", "table"),
                        Map.of("comp", "list", "queries", Set.of(Map.of("datasourceId", datasource.getId()))),
                        null))
                .delayUntil(__ -> deleteMono)
                .flatMap(createApplicationRequest -> applicationApiService.create(createApplicationRequest))
                .flatMap(applicationView -> {
                    Application application = Application.builder()
                            .editingApplicationDSL(applicationView.getApplicationDSL())
                            .name("app03")
                            .build();
                    return applicationApiService.update(applicationView.getApplicationInfoView().getApplicationId(), application);
                });

        StepVerifier.create(applicationViewMono)
                .expectErrorMatches(throwable -> throwable instanceof BizException bizException
                        && bizException.getError() == BizError.NOT_AUTHORIZED
                        && bizException.getMessageKey().equals("APPLICATION_EDIT_ERROR_LACK_OF_DATASOURCE_PERMISSIONS"))
                .verify();
    }
}