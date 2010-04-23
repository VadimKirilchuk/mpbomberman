package org.amse.bomberman.client.view.mywizard;

import javax.swing.JOptionPane;
import org.amse.bomberman.client.control.impl.Controller;
import org.amse.bomberman.client.net.NetException;

/**
 *
 * @author Michael Korovkin
 */
public class PanelDescriptor3 extends PanelDescriptor {
    public PanelDescriptor3(Wizard wizard, String identifier) {
        super(wizard, identifier, new Panel3());
    }

    @Override
    public void doBeforeDisplay() {
        try {
            Panel3 panel3 = (Panel3) this.getPanel();
            panel3.clean();
            Controller.getInstance().requestGameInfo();
        } catch (NetException ex) {
            JOptionPane.showMessageDialog(this.getWizard(), "Connection was lost.\n"
                    + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            this.getWizard().setCurrentJPanel(BombWizard.IDENTIFIER1);
        }
    }

    @Override
    public void goBack() {
        try {
            Controller.getInstance().requestLeaveGame();
        } catch (NetException ex) {
             JOptionPane.showMessageDialog(this.getWizard(), "Connection was lost.\n"
                    + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
             this.getWizard().setCurrentJPanel(BombWizard.IDENTIFIER1);
        }
    }
}
