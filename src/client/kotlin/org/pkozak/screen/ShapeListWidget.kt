package org.pkozak.screen

import com.google.common.collect.Lists
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.ElementListWidget
import org.pkozak.Shape

class ShapeListWidget(
    parent: ShapesScreen,
    client: MinecraftClient,
    width: Int,
    height: Int,
    y: Int,
    itemHeight: Int,
    shapes: List<Shape>
) : ElementListWidget<ShapeListEntry>(client, width, height, y, itemHeight) {

    private val shapeListEntries: MutableList<ShapeListEntry> = mutableListOf()

    init {
        for (shape in shapes) {
            addEntry(ShapeListEntry(shape, client))
        }
    }

    public override fun addEntry(entry: ShapeListEntry): Int {
        super.addEntry(entry)
        shapeListEntries.add(entry)
        return shapeListEntries.size - 1
    }
}
