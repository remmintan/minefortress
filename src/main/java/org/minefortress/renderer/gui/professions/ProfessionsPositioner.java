package org.minefortress.renderer.gui.professions;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ProfessionsPositioner {
    private final ProfessionsPositioner parent;
    private final ProfessionsPositioner previousSibling;
    private final ProfessionWidget professionWidget;
    private final int childrenSize;
    private final List<ProfessionsPositioner> children = Lists.newArrayList();
    private ProfessionsPositioner optionalLast;
    private ProfessionsPositioner substituteChild;
    private int depth;
    private float row;
    private float relativeRowInSiblings;
    private float posXSib;
    private float posX;

    public ProfessionsPositioner(ProfessionWidget widget, ProfessionsPositioner parent, ProfessionsPositioner previousSibling, int childrenSize, int depth) {
//        if(!widget.isUnlocked())  throw new IllegalArgumentException("Can't position locked profession");
        this.professionWidget = widget;
        this.parent = parent;
        this.previousSibling = previousSibling;
        this.childrenSize = childrenSize;
        this.optionalLast = this;
        this.depth = depth;
        this.row = -1.0f;

        ProfessionsPositioner professionPositioner = null;
        for (ProfessionWidget child : widget.getChildren()) {
            professionPositioner = this.findChildrenRecursively(child, professionPositioner);
        }
    }

    private ProfessionsPositioner findChildrenRecursively(ProfessionWidget profession, ProfessionsPositioner lastChild) {
        final ProfessionWidget parent = profession.getParent();
        if(parent == null || parent.isUnlocked()) {
            lastChild = new ProfessionsPositioner(profession, this, lastChild, this.children.size() + 1, this.depth + 1);
            this.children.add(lastChild);
        } else {
            for(ProfessionWidget widget : profession.getChildren()) {
                lastChild = this.findChildrenRecursively(widget, lastChild);
            }
        }

        return lastChild;
    }

    private void calculateRecursively() {
        if (this.children.isEmpty()) {
            this.row = this.previousSibling != null ? this.previousSibling.row + 1.0f : 0.0f;
            return;
        }
        ProfessionsPositioner professionPositioner = null;
        for (ProfessionsPositioner child : this.children) {
            child.calculateRecursively();
            professionPositioner = child.onFinishCalculation(professionPositioner == null ? child : professionPositioner);
        }
        this.onFinishChildrenCalculation();
        float f = (this.children.get((int)0).row + this.children.get((int)(this.children.size() - 1)).row) / 2.0f;
        if (this.previousSibling != null) {
            this.row = this.previousSibling.row + 1.0f;
            this.relativeRowInSiblings = this.row - f;
        } else {
            this.row = f;
        }
    }

    private float findMinRowRecursively(float deltaRow, int depth, float minRow) {
        this.row += deltaRow;
        this.depth = depth;
        if (this.row < minRow) {
            minRow = this.row;
        }
        for (ProfessionsPositioner professionPositioner : this.children) {
            minRow = professionPositioner.findMinRowRecursively(deltaRow + this.relativeRowInSiblings, depth + 1, minRow);
        }
        return minRow;
    }

    private void increaseRowRecursively(float deltaRow) {
        this.row += deltaRow;
        for (ProfessionsPositioner professionPositioner : this.children) {
            professionPositioner.increaseRowRecursively(deltaRow);
        }
    }

    private void onFinishChildrenCalculation() {
        float x = 0.0f;
        float xSib = 0.0f;
        for (int i = this.children.size() - 1; i >= 0; --i) {
            ProfessionsPositioner professionPositioner = this.children.get(i);
            professionPositioner.row += x;
            professionPositioner.relativeRowInSiblings += x;
            x += professionPositioner.posX + (xSib += professionPositioner.posXSib);
        }
    }

    @Nullable
    private ProfessionsPositioner getFirstChild() {
        if (this.substituteChild != null) {
            return this.substituteChild;
        }
        if (!this.children.isEmpty()) {
            return this.children.get(0);
        }
        return null;
    }

    @Nullable
    private ProfessionsPositioner getLastChild() {
        if (this.substituteChild != null) {
            return this.substituteChild;
        }
        if (!this.children.isEmpty()) {
            return this.children.get(this.children.size() - 1);
        }
        return null;
    }

    private ProfessionsPositioner onFinishCalculation(ProfessionsPositioner last) {
        if (this.previousSibling == null) {
            return last;
        }
        ProfessionsPositioner professionPositioner = this;
        ProfessionsPositioner professionPositioner2 = this;
        ProfessionsPositioner professionPositioner3 = this.previousSibling;
        ProfessionsPositioner professionPositioner4 = this.parent.children.get(0);
        float f = this.relativeRowInSiblings;
        float g = this.relativeRowInSiblings;
        float h = professionPositioner3.relativeRowInSiblings;
        float i = professionPositioner4.relativeRowInSiblings;
        while (professionPositioner3.getLastChild() != null && professionPositioner.getFirstChild() != null) {
            professionPositioner3 = professionPositioner3.getLastChild();
            professionPositioner = professionPositioner.getFirstChild();
            professionPositioner4 = professionPositioner4.getFirstChild();
            professionPositioner2 = professionPositioner2.getLastChild();
            professionPositioner2.optionalLast = this;
            float j = professionPositioner3.row + h - (professionPositioner.row + f) + 1.0f;
            if (j > 0.0f) {
                professionPositioner3.getLast(this, last).pushDown(this, j);
                f += j;
                g += j;
            }
            h += professionPositioner3.relativeRowInSiblings;
            f += professionPositioner.relativeRowInSiblings;
            i += professionPositioner4.relativeRowInSiblings;
            g += professionPositioner2.relativeRowInSiblings;
        }
        if (professionPositioner3.getLastChild() != null && professionPositioner2.getLastChild() == null) {
            professionPositioner2.substituteChild = professionPositioner3.getLastChild();
            professionPositioner2.relativeRowInSiblings += h - g;
        } else {
            if (professionPositioner.getFirstChild() != null && professionPositioner4.getFirstChild() == null) {
                professionPositioner4.substituteChild = professionPositioner.getFirstChild();
                professionPositioner4.relativeRowInSiblings += f - i;
            }
            last = this;
        }
        return last;
    }

    private void pushDown(ProfessionsPositioner positioner, float extraRowDistance) {
        float f = positioner.childrenSize - this.childrenSize;
        if (f != 0.0f) {
            positioner.posXSib -= extraRowDistance / f;
            this.posXSib += extraRowDistance / f;
        }
        positioner.posX += extraRowDistance;
        positioner.row += extraRowDistance;
        positioner.relativeRowInSiblings += extraRowDistance;
    }

    private ProfessionsPositioner getLast(ProfessionsPositioner professionPositioner, ProfessionsPositioner professionPositioner2) {
        if (this.optionalLast != null && professionPositioner.parent.children.contains(this.optionalLast)) {
            return this.optionalLast;
        }
        return professionPositioner2;
    }

    private void apply() {
        this.professionWidget.setPos(this.depth, this.row);

        if (!this.children.isEmpty()) {
            for (ProfessionsPositioner professionPositioner : this.children) {
                professionPositioner.apply();
            }
        }
    }

    static void arrangeForTree(ProfessionWidget root) {
//        if(!root.isUnlocked()) throw new IllegalArgumentException("Cannot arrange for tree on locked profession");
        ProfessionsPositioner professionPositioner = new ProfessionsPositioner(root, null, null, 1, 0);
        professionPositioner.calculateRecursively();
        float f = professionPositioner.findMinRowRecursively(0.0f, 0, professionPositioner.row);
        if (f < 0.0f) {
            professionPositioner.increaseRowRecursively(-f);
        }
        professionPositioner.apply();
    }
}
