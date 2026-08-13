package io.github.isoyigido.bluff.gui.components;

import io.github.isoyigido.basic.gui.core.Component;

import java.util.Arrays;

public final class VerticalContainer extends Component {
    public VerticalContainer(int gap, boolean center, Component... components) {
        int maxWidth = Arrays.stream(components).mapToInt(Component::getWidth).max().orElse(0);
        int halfMaxWidth = maxWidth / 2;

        int totalHeight = 0;

        for (Component component : components) {
            this.addWidget(center ? component.top(halfMaxWidth, totalHeight) : component.topLeft(0, totalHeight));

            totalHeight += component.getHeight() + gap;
        }

        totalHeight -= gap;

        this.setWidth(maxWidth);
        this.setHeight(totalHeight);
    }
}