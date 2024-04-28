package com.example.installer_agent;

import java.awt.*;
import java.util.function.Predicate;

/**
 * Interface for filtering {@link Component} objects.
 */
@FunctionalInterface
public interface ComponentFilter extends Predicate<Component> {

    /**

