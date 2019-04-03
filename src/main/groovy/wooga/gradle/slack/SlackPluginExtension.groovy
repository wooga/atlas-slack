package wooga.gradle.slack

import org.gradle.api.provider.Property

interface SlackPluginExtension {

    Property<URL> getWebhook()

    void setWebhook(URL url)
    void setWebhook(String url)

    Property<String> getUsername()

    Property<URL> getIcon()
    void setIcon(URL url)
    void setIcon(String url)

}