package com.smallaswater.npc.route;

import cn.nukkit.math.Vector3;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author lt_name
 */
@Setter
@Getter
@ToString(of = "vector3")
public class Node {

    private Vector3 vector3;
    private Node parent;
    private double G;
    private double H;
    private double F;
    
    public Node(@NotNull Vector3 vector3) {
        this(vector3, null);
    }
    
    public Node(@NotNull Vector3 vector3, Node parent) {
        this(vector3, parent, 0, 0);
    }

    public Node(@NotNull Vector3 vector3, Node parent, double G, double H) {
        this.vector3 = vector3;
        this.parent = parent;
        this.G = G;
        this.H = H;
        this.F = G + H;
    }
    
    @Override
    public boolean equals(Object o) {
        return this.equals(o, false);
    }
    
    public boolean equals(Object o, boolean checkParent) {
        if (o instanceof Node) {
            if (((Node) o).getVector3().equals(this.getVector3())) {
                if (checkParent) {
                    return ((Node) o).getParent().equals(this.getParent());
                }
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(vector3, parent);
    }
    
}
