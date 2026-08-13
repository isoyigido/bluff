package io.github.isoyigido.bluff.gui.components;

import io.github.isoyigido.basic.gui.core.Component;

import java.util.Arrays;

public final class HorizontalContainer extends Component {
    public HorizontalContainer(int gap, boolean center, Component... components) {
        int maxHeight = Arrays.stream(components).mapToInt(Component::getHeight).max().orElse(0);
        int halfMaxHeight = maxHeight / 2;

        int totalWidth = 0;

        for (Component component : components) {
            this.addWidget(center ? component.left(totalWidth, halfMaxHeight) : component.topLeft(totalWidth, 0));

            totalWidth += component.getWidth() + gap;
        }

        totalWidth -= gap;

        this.setWidth(totalWidth);
        this.setHeight(maxHeight);
    }
}