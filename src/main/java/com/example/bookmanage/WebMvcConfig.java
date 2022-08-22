package com.example.bookmanage;

import java.util.Arrays;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 書籍管理システムのConfiguration
 *
 * 以下を実装している。
 * PUT/DELETEをPOSTするためにHiddenHttpMehotdFilterをFilterとして設定する。
 * validationで使用するメッセージプロパティのエンコードをUTF-8に設定する。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * HiddenHttpMehotdFilterをFilterに設定するためのBeanを返却する。
     *
     * @return HiddenHttpMehotdFilterをFilterに設定するためのBean
     */
    @Bean
    public FilterRegistrationBean<HiddenHttpMethodFilter> hiddenHttpMethodFilter() {
        HiddenHttpMethodFilter filter = new HiddenHttpMethodFilter();
        FilterRegistrationBean<HiddenHttpMethodFilter> filterRegBean = new FilterRegistrationBean<>(filter);
        filterRegBean.setUrlPatterns(Arrays.asList("/*"));
        return filterRegBean;
    }

    /**
     * validationで使用するメッセージプロパティのエンコードにUTF-8を設定したLocalValidatorFactoryBeanを返却する。
     * 
     * @return LocalValidatorFactoryBean
     */
    @Override
    public Validator getValidator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:ValidationMessages");
        messageSource.setDefaultEncoding("UTF-8");
        validator.setValidationMessageSource(messageSource);
        return validator;
    }

}
