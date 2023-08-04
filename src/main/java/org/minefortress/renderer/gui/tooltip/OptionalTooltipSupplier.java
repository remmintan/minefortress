package org.minefortress.renderer.gui.tooltip;

import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.OrderedText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class OptionalTooltipSupplier extends BaseTooltipSupplier{

    @Nullable
    private Supplier<Optional<String>> tooltipTextSupplier;

    public OptionalTooltipSupplier() {
    }

    public void provideTooltipText(Supplier<Optional<String>> tooltipTextSupplier) {
        this.tooltipTextSupplier = tooltipTextSupplier;
    }

    @NotNull
    @Override
    protected List<OrderedText> getTooltip() {
        if(tooltipTextSupplier == null) {
            return List.of();
        }
        return tooltipTextSupplier.get()
                .map(LiteralTextContent::new)
                .map(LiteralTextContent::asOrderedText)
                .map(List::of)
                .orElse(List.of());
    }
}
