package wooga.gradle.slack.internal

import org.gradle.api.Project
import org.gradle.api.provider.Property
import wooga.gradle.slack.SlackPluginExtension

class DefaultSlackPluginExtension implements SlackPluginExtension {

    final Property<URL> webhook
    final Property<String> username
    final Property<URL> icon

    @Override
    void setWebhook(URL url) {
        webhook.set(url)
    }

    @Override
    void setWebhook(String url) {
        setWebhook(new URL(url))
    }

    @Override
    void setIcon(URL url) {
        icon.set(url)
    }

    @Override
    void setIcon(String url) {
        setIcon(new URL(url))
    }

    DefaultSlackPluginExtension(Project project) {
        this.webhook = project.objects.property(URL)
        this.username = project.objects.property(String)
        this.icon = project.objects.property(URL)
    }
}
