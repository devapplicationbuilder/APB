package org.quickdev.domain.template.service;

import java.util.Collection;

import org.quickdev.domain.template.model.Template;
import org.quickdev.domain.template.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class TemplateService {

    @Autowired
    private TemplateRepository templateRepository;

    public Mono<Template> getById(String templateId) {
        return templateRepository.findById(templateId);
    }

    public Flux<Template> getByApplicationIds(Collection<String> applicationIds) {
        return templateRepository.findByApplicationIdIn(applicationIds);
    }

    public Mono<Template> getByApplicationId(String applicationId) {
        return templateRepository.findByApplicationId(applicationId);
    }
}
