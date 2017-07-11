package me.robin.crawler.web.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;

/**
 * Created by Lubin.Xuan on 2017-06-27.
 */
@Data
@ConfigurationProperties(prefix = "swagger")
public class SwaggerProperties {

    private String basePackage;

    private _ApiInfo apiInfo;

    private _Contact contact;

    @Data
    public static class _ApiInfo {
        private String version;
        private String title;
        private String description;
        private String termsOfServiceUrl;
        private String license;
        private String licenseUrl;

        public ApiInfo apiInfo(Contact contact) {
            return new ApiInfo(title, description, version, termsOfServiceUrl, contact, license, licenseUrl);
        }
    }

    @Data
    public static class _Contact {
        private String name;
        private String url;
        private String email;

        public Contact contact() {
            return new Contact(name, url, email);
        }
    }
}
