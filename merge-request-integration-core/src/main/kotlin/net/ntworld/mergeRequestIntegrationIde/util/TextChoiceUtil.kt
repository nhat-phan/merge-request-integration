package net.ntworld.mergeRequestIntegrationIde.util

object TextChoiceUtil {
    fun comment(count: Int): String {
        return if (count < 2) "$count comment" else "$count comments"
    }

    fun reply(count: Int): String {
        return if (count < 2) "$count reply" else "$count replies"
    }

    fun draft(count: Int): String {
        return if (count < 2) "$count draft" else "$count drafts"
    }

    fun draftComment(count: Int): String {
        return if (count < 2) "$count draft comment" else "$count draft comments"
    }

    fun commentWithDraft(totalCount: Int, draftCount: Int): String {
        return if (draftCount > 0) comment(totalCount) + " - ${draft(draftCount)}" else comment(totalCount)
    }

    fun replyWithDraft(totalCount: Int, draftCount: Int): String {
        return if (draftCount > 0) reply(totalCount) + " - ${draft(draftCount)}" else reply(totalCount)
    }
}