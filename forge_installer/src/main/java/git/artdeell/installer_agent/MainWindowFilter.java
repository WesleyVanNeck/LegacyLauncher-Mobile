package com.example.installer_agent;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * A component filter that checks if a component is a JRadioButton, JTextField, or JButton.
 */
public class MainWindowFilter implements ComponentFilter {

    /**
     * Checks if a component is a JRadioButton, JTextField, or JButton.
     *
     * @param component the component to check
     * @return true if the component is an instance of JRadioButton, JTextField, or JButton, false otherwise
     */
    @Override
    public boolean checkComponent(Component component) {
        if (Objects.isNull(component)) {
            throw new NullPointerException("Component cannot be null");
        }

        return component instanceof JRadioButton
                || component instanceof JTextField
                || component instanceof JButton;
    }
}
