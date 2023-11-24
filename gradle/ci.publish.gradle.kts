gradle.afterProject {
    if (name == "prefiller") {
        extensions.configure<org.gradle.api.publish.PublishingExtension>() {
            repositories {
                maven {
                    name = "PixnewsS3"
                    setUrl("s3://maven.pixnews.ru/")
                    credentials(AwsCredentials::class) {
                        accessKey = providers.environmentVariable("YANDEX_S3_ACCESS_KEY_ID").getOrElse("")
                        secretKey = providers.environmentVariable("YANDEX_S3_SECRET_ACCESS_KEY").getOrElse("")
                    }
                }
            }
        }
    }
}
