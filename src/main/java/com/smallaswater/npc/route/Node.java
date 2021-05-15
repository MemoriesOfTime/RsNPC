package com.smallaswater.npc.route;

import cn.nukkit.math.Vector3;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 * @author lt_name
 */
@Setter
@Getter
@EqualsAndHashCode
public class Node {

    private Vector3 vector3;
    private Node parent;
    
    public Node(@NotNull Vector3 vector3) {
        this(vector3, null);
    }
    
    public Node(@NotNull Vector3 vector3, Node parent) {
        this.vector3 = vector3;
        this.parent = parent;
    }
    
}
