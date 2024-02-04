package org.pkozak.screen

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.ElementListWidget
import org.pkozak.Shape

class ShapeListWidget(
    private val parent: ShapesScreen,
    client: MinecraftClient,
    width: Int,
    height: Int,
    y: Int,
    itemHeight: Int,
    private val shapes: MutableList<Shape>
) : ElementListWidget<ShapeListEntry>(client, width, height, y, itemHeight) {

    private val shapeListEntries: MutableList<ShapeListEntry> = mutableListOf()

    init {
        this.setRenderBackground(false)
        for (shape in shapes) {
            addEntry(ShapeListEntry(this, shape, client))
        }
    }

    public override fun addEntry(entry: ShapeListEntry): Int {
        super.addEntry(entry)
        shapeListEntries.add(entry)
        return shapeListEntries.size - 1
    }

    public override fun removeEntry(entry: ShapeListEntry): Boolean {
        shapes.remove(entry.shape)
        shapeListEntries.remove(entry)
        return super.removeEntry(entry)
    }
}
