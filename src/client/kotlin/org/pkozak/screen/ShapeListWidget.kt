package org.pkozak.screen

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.ElementListWidget
import org.pkozak.shape.Shape
import kotlin.math.max

class ShapeListWidget(
    internal val parent: ShapesScreen,
    client: MinecraftClient,
    width: Int,
    height: Int,
    y: Int,
    itemHeight: Int,
    private val shapes: MutableList<Shape>
) : ElementListWidget<ShapeListEntry>(client, width, height, y, itemHeight) {

    private val shapeListEntries: MutableList<ShapeListEntry> = mutableListOf()

    init {
        for (shape in shapes) {
            addEntry(ShapeListEntry(this, shape, client))
        }
    }

    override fun getRowLeft(): Int {
        return this.x + 6
    }

    override fun getRowWidth(): Int {
        return this.width - (if (max(0.0, (this.maxPosition - (this.bottom - this.y - 4)).toDouble()) > 0) 18 else 12)
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
