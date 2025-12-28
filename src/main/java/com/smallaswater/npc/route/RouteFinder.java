package com.smallaswater.npc.route;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockFence;
import cn.nukkit.block.BlockFenceGate;
import cn.nukkit.level.Level;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.AsyncTask;
import com.smallaswater.npc.RsNPC;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author lt_name
 */
@Getter
public class RouteFinder {
    
    private final int startTick;
    
    private boolean processingComplete = false;
    
    private final Level level;
    private final Vector3 start;
    private final Vector3 end;
    private final int distance;

    LinkedList<Node> openNodes = new LinkedList<>();
    ArrayList<Vector3> closeNodes = new ArrayList<>();

    LinkedList<Node> nodes = new LinkedList<>();
    
    public RouteFinder(@NotNull Level level, @NotNull Vector3 start, @NotNull Vector3 end) {
        this(level, start, end, true);
    }
    
    public RouteFinder(@NotNull Level level, @NotNull Vector3 start, @NotNull Vector3 end, boolean async) {
        this.startTick = Server.getInstance().getTick();
        this.level = level;
        this.start = start.floor();
        this.end = end.floor();
    
        this.distance = (int) start.distance(end);

        if (async) {
            Server.getInstance().getScheduler().scheduleAsyncTask(RsNPC.getInstance(), new AsyncTask() {
                @Override
                public void onRun() {
                    process();
                }
            });
        }else {
            this.process();
        }
    }

    /**
     * 寻路
     */
    private void process() {
        this.openNodes.add(new Node(this.start));
        
        Node nowNode;
        while ((nowNode = this.openNodes.poll()) != null && Server.getInstance().isRunning()) {
            //到达终点，保存路径
            if (nowNode.getVector3().equals(this.getEnd())) {
                Node parent = nowNode;
                parent.setVector3(parent.getVector3().add(0.5, 0, 0.5));
                this.nodes.add(parent);
                while ((parent = parent.getParent()) != null) {
                    parent.setVector3(parent.getVector3().add(0.5, 0, 0.5));
                    this.nodes.addFirst(parent);
                }
                break;
            }
    
            //超时跳出 (60s)
            if (Server.getInstance().getTick() - this.startTick > 20 * 60) {
                break;
            }
    
            LinkedList<Node> nextNodes = new LinkedList<>();

            // 顺序：先尝试平面移动(0)，然后下落(-1)，最后跳跃(1)
            for (int y : new int[]{0, -1, 1}) {
                boolean N = this.check(nowNode, nextNodes, 0, y, -1);
                boolean E = this.check(nowNode, nextNodes, 1, y, 0);
                boolean S = this.check(nowNode, nextNodes, 0, y, 1);
                boolean W = this.check(nowNode, nextNodes, -1, y, 0);

                if (N && E) {
                    this.check(nowNode, nextNodes, 1, y, -1);
                }
                if (E && S) {
                    this.check(nowNode, nextNodes, 1, y, 1);
                }
                if (W && S) {
                    this.check(nowNode, nextNodes, -1, y, 1);
                }
                if (W && N) {
                    this.check(nowNode, nextNodes, -1, y, -1);
                }
            }
    
            if (nextNodes.isEmpty()) {
                continue;
            }
            
            this.openNodes.addAll(nextNodes);
            this.openNodes.sort((o1, o2) -> {
                double d1 = o1.getF();
                double d2 = o2.getF();
                if (d1 == d2) {
                    return 0;
                }
                return d1 > d2 ? 1 : -1;
            });
        }
        
        this.processingComplete = true;
    }
    
    private boolean check(Node nowNode, LinkedList<Node> nextNodes, int x, int y, int z) {
        Vector3 vector3 = nowNode.getVector3().add(x, y, z);
        if (this.closeNodes.contains(vector3)) {
            return false;
        }
        this.closeNodes.add(vector3);

        double moveCost = 1.0;
        if (x != 0 && z != 0) {
            moveCost = 1.414; // sqrt(2)
        }
        if (y != 0) {
            moveCost += 0.5;
        }

        double G = nowNode.getG() + moveCost;
        double H = vector3.distance(this.end);

        Node nextNode = new Node(vector3, nowNode, G, H);
        if (this.canMoveTo(nowNode, nextNode)) {
            nextNodes.add(nextNode);
            return true;
        }
        return false;
    }
    
    /**
     * 检查是否可以移动到目标节点
     *
     * @param nowNode 当前节点
     * @param target 目标节点
     * @return 是否可以移动到目标节点
     */
    private boolean canMoveTo(Node nowNode, Node target) {
        // 基本检查：目标位置和头顶必须可通过
        if (!this.getBlockFast(target).canPassThrough() ||
                !this.getBlockFast(target.getVector3().add(0, 1, 0)).canPassThrough()) {
            return false;
        }

        // 脚下必须可以站立
        if (!this.canWalkOn(this.getBlockFast(target.getVector3().add(0, -1, 0)))) {
            return false;
        }

        int yDiff = (int) (target.getVector3().getY() - nowNode.getVector3().getY());

        // 向上跳跃检查
        if (yDiff > 0) {
            // 当前位置头顶需要有足够空间
            if (!this.getBlockFast(nowNode.getVector3().add(0, 2, 0)).canPassThrough()) {
                return false;
            }

            // 检查跳跃路径中间是否有阻挡
            for (int i = 1; i < yDiff; i++) {
                Vector3 midPos = nowNode.getVector3().add(0, i, 0);
                if (!this.getBlockFast(midPos).canPassThrough() ||
                    !this.getBlockFast(midPos.add(0, 1, 0)).canPassThrough()) {
                    return false;
                }
            }
        }

        // 向下移动检查
        if (yDiff < 0) {
            // 目标位置头顶需要有空间
            if (!this.getBlockFast(target.getVector3().add(0, 2, 0)).canPassThrough()) {
                return false;
            }

            // 检查下落路径是否畅通
            for (int i = -1; i > yDiff; i--) {
                Vector3 midPos = nowNode.getVector3().add(0, i, 0);
                if (!this.getBlockFast(midPos).canPassThrough() ||
                    !this.getBlockFast(midPos.add(0, 1, 0)).canPassThrough()) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean canWalkOn(Block block) {
        if (block.getId() == Block.LAVA || block.getId() == Block.STILL_LAVA || block.getId() == Block.CACTUS) {
            return false;
        }
        if (block instanceof BlockFence || block instanceof BlockFenceGate) {
            return false;
        }
        if (block.getId() == Block.STILL_WATER || block.getId() == Block.WATER) {
            return true;
        }
        return !block.canPassThrough();
    }

    /**
     * 用粒子显示路径
     */
    public void show() {
        for (Node node : this.nodes) {
            this.level.addParticleEffect(node.getVector3(), ParticleEffect.REDSTONE_TORCH_DUST);
            this.level.addParticleEffect(node.getVector3().add(0.1, 0, 0.1), ParticleEffect.REDSTONE_TORCH_DUST);
            this.level.addParticleEffect(node.getVector3().add(0.1, 0, -0.1), ParticleEffect.REDSTONE_TORCH_DUST);
            this.level.addParticleEffect(node.getVector3().add(-0.1, 0, 0.1), ParticleEffect.REDSTONE_TORCH_DUST);
            this.level.addParticleEffect(node.getVector3().add(-0.1, 0, -0.1), ParticleEffect.REDSTONE_TORCH_DUST);
        }
    }

    /**
     * 快速获取方块
     *
     * @param node 节点
     * @return 方块
     */
    public Block getBlockFast(Node node) {
        return this.getBlockFast(node.getVector3());
    }

    /**
     * 快速获取方块
     *
     * @param vector3 位置
     * @return 方块
     */
    public Block getBlockFast(Vector3 vector3) {
        return this.getBlockFast(vector3.getFloorX(), vector3.getFloorY(), vector3.getFloorZ());
    }

    /**
     * 快速获取方块
     *
     * @param x 坐标x
     * @param y 坐标y
     * @param z 坐标z
     * @return 方块
     */
    public Block getBlockFast(int x, int y, int z) {
        if (!"Nukkit".equals(Server.getInstance().getName())) {
            return this.level.getBlock(x, y, z);
        }
        int fullState = 0;
        if (y >= 0 && y < 256) {
            int cx = x >> 4;
            int cz = z >> 4;
            BaseFullChunk chunk = this.getLevel().getChunk(cx, cz);
            
            if (chunk != null) {
                fullState = chunk.getFullBlock(x & 15, y, z & 15);
            }
        }
        
        Block block = Block.fullList[fullState & 4095].clone();
        block.x = x;
        block.y = y;
        block.z = z;
        block.level = this.getLevel();
        return block;
    }
    
}
