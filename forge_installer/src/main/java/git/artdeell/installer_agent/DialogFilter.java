package git.artdeell.installer_agent;

import javax.swing.*;
import java.util.function.Predicate;

/**
 * A predicate that tests if a component is a {@code JOptionPane} or a {@code JProgressBar}.
 * This predicate is intended to be used with Swing components in a {@code JDialog}.
 */
public final class JDialogComponentFilter implements Predicate<Component> {

    private JDialogComponentFilter() {}

    @Override
    public boolean test(Component component) {
        return component instanceof JOptionPane
                || component instanceof JProgressBar;
    }

    /**
     * A factory method that creates a new instance of {@code JDialogComponentFilter}.
     *
     * @return a new instance of {@code JDialogComponentFilter}
     */
    public static JDialogComponentFilter createFilter() {
        return new JDialogComponentFilter();
    }
}
