package net.ntworld.mergeRequestIntegrationIde.comments

import com.intellij.diff.util.Side
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vfs.VirtualFile
import net.ntworld.mergeRequest.Comment

class CodeReviewCommentsManager {

    //создать комментарий
    //получить все комментарии для файла
    //джампнуть в файл из комментария
    //

    private val comments : MutableList<Comment> = mutableListOf()
    private val commentsToPath : MutableMap<String, Comment> = mutableMapOf()

    private val commentsToChange: MutableMap<Change, Comment> = mutableMapOf()

    fun getCommentsForFile(file : VirtualFile) {

    }

    /**
     * Creates comment for given change
     *
     * на вход принимаем какой чейнджь на какой стороне и тд, после открываем компонент в котором можно написать коммент
     * в компоненте помещаем хук который позволит сохранить комментарий
     *
     */
    fun createComment(change: Change, side: Side, line: Int) {

    }

    fun replyForComment(parent: Comment, reply: Comment) {

    }
}