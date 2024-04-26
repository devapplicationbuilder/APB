package org.quickdev.api.service;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickdev.api.application.view.ApplicationInfoView;
import org.quickdev.api.application.view.ApplicationPermissionView;
import org.quickdev.api.common.mockuser.WithMockUser;
import org.quickdev.api.home.FolderApiService;
import org.quickdev.api.permission.view.PermissionItemView;
import org.quickdev.domain.folder.model.Folder;
import org.quickdev.domain.folder.service.FolderService;
import org.quickdev.domain.permission.model.ResourceRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FolderApiServiceTest {

    @Autowired
    private FolderApiService folderApiService;
    @Autowired
    private FolderService folderService;

    @Test
    @WithMockUser
    public void create() {
        Folder folder = new Folder();
        folder.setParentFolderId(null);
        folder.setName("root");
        StepVerifier.create(folderApiService.create(folder))
                .assertNext(f -> Assert.assertNotNull(f.getFolderId()))
                .verifyComplete();
    }

    @Test
    @WithMockUser
    public void delete() {

        String id = "folder03";

        StepVerifier.create(folderService.exist(id))
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(folderApiService.delete(id))
                .assertNext(folder -> Assert.assertEquals(id, folder.getId()))
                .verifyComplete();

        StepVerifier.create(folderService.exist(id))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @WithMockUser
    public void update() {
        String id = "folder02";

        StepVerifier.create(folderService.findById(id))
                .assertNext(folder -> assertEquals("folder02", folder.getName()))
                .verifyComplete();

        Folder newFolder = new Folder();
        newFolder.setId(id);
        newFolder.setName("test_update");
        StepVerifier.create(folderApiService.update(newFolder))
                .assertNext(Assert::assertNotNull)
                .verifyComplete();

        StepVerifier.create(folderService.findById(id))
                .assertNext(folder -> assertEquals("test_update", folder.getName()))
                .verifyComplete();
    }

    @Test
    @WithMockUser
    public void move() {

        Mono<? extends List<?>> mono = folderApiService.move("app01", "folder02")
                .then(folderApiService.getElements("folder02", null).collectList());

        StepVerifier.create(mono)
                .assertNext(list -> {
                    assertEquals(1, list.size());
                    ApplicationInfoView applicationInfoView = (ApplicationInfoView) list.get(0);
                    assertEquals("app01", applicationInfoView.getApplicationId());
                })
                .verifyComplete();
    }

    @Test
    @WithMockUser
    public void grantPermission() {

        Mono<ApplicationPermissionView> mono =
                folderApiService.grantPermission("folder02", Set.of("user02"), Set.of("group01"), ResourceRole.OWNER)
                        .then(folderApiService.getPermissions("folder02"));

        StepVerifier.create(mono)
                .assertNext(applicationPermissionView -> {
                    assertEquals(2, applicationPermissionView.getPermissions().size());
                    Set<String> ids = applicationPermissionView.getPermissions().stream()
                            .map(PermissionItemView::getId)
                            .collect(Collectors.toSet());
                    assertEquals(Set.of("user02", "group01"), ids);
                })
                .verifyComplete();
    }
}