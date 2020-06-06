package net.ntworld.mergeRequestIntegrationIde.component.dialog;

import net.ntworld.mergeRequestIntegrationIde.Component
import javax.swing.*;

class LegalWarningDialog: Component {
    var myWrapper: JPanel? = null
    var myLegalWarning: JLabel? = null
    var myWords1: JLabel? = null
    var myWords2: JLabel? = null
    var myWords3: JLabel? = null

    override val component: JComponent = myWrapper!!
}
