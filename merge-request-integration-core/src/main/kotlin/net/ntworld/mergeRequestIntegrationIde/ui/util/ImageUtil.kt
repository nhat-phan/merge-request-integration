package net.ntworld.mergeRequestIntegrationIde.ui.util

import com.intellij.openapi.util.IconLoader
import com.intellij.util.IconUtil
import com.intellij.util.ui.JBUI
import java.net.URL
import javax.swing.Icon

object ImageUtil {

    fun loadIconFromUrl(url: String): Icon? {
        if (url.isEmpty()) {
            return null
        }
        return IconLoader.findIcon(URL(url), true)
    }

    fun loadIconFromUrl(url: String, resourceFallback: String): Icon {
        val icon = loadIconFromUrl(url)
        if (null === icon) {
            return IconLoader.getIcon(resourceFallback, ImageUtil.javaClass)
        }
        return icon
    }

    fun loadIconFromUrl(url: String, size: Int): Icon? {
        val icon = loadIconFromUrl(url)
        if (null !== icon) {
            return resize(icon, size)
        }
        return null
    }

    fun loadIconFromUrl(url: String, resourceFallback: String, size: Int): Icon {
        val icon = loadIconFromUrl(url, resourceFallback)
        return if (icon.iconWidth >= size) {
            resize(icon, size)
        } else {
            resize(IconLoader.getIcon(resourceFallback, ImageUtil.javaClass), size)
        }
    }

    private fun resize(icon: Icon, size: Int): Icon {
        val scale = JBUI.scale(size).toFloat() / icon.iconWidth.toFloat()
        return IconUtil.scale(icon, null, scale)
    }
}