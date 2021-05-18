package com.smallaswater.npc.route;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.level.Level;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.AsyncTask;
import com.smallaswater.npc.RsNpcX;
import com.smallaswater.npc.entitys.EntityRsNpc;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author lt_name
 */
@Getter
public class RouteFinder {
    
    private int startTick;
    
    private boolean processingComplete = false;
    
    private final Level level;
    private final Vector3 start;
    private final Vector3 end;
    private final int distance;
    private Entity entity;
    
    LinkedList<Node> nodes = new LinkedList<>();
    
    public RouteFinder(@NotNull Level level, @NotNull Vector3 start, @NotNull Vector3 end) {
        this(level, start, end, null);
    }
    
    public RouteFinder(@NotNull Level level, @NotNull Vector3 start, @NotNull Vector3 end, Entity entity) {
        this.startTick = Server.getInstance().getTick();
        this.level = level;
        this.start = start.floor();
        this.end = end.floor();
    
        this.distance = (int) start.distance(end);
        
        this.entity = entity;
        if (this.entity == null) {
            Position position = Position.fromObject(start, level);
            this.entity = new EntityHuman(position.getChunk(),
                    Entity.getDefaultNBT(position).putCompound("Skin", new CompoundTag()));
        }
    
        Server.getInstance().getScheduler().scheduleAsyncTask(RsNpcX.getInstance(), new AsyncTask() {
            @Override
            public void onRun() {
                process();
                if (entity instanceof EntityRsNpc) {
                    ((EntityRsNpc) entity).getNodes().addAll(nodes);
                    ((EntityRsNpc) entity).setLockRoute(false);
                }
            }
        });
    }
    
    private void process() {
        LinkedList<Node> needChecks = new LinkedList<>();
        ArrayList<Vector3> completeList = new ArrayList<>();
        needChecks.add(new Node(this.start));
        
        Node nowNode;
        while ((nowNode = needChecks.poll()) != null) {
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
            
            for (int y = -1; y <= 1; y++) {
                boolean N = this.check(nowNode, nextNodes, completeList, 0, y, -1);
                boolean E = this.check(nowNode, nextNodes, completeList, 1, y, 0);
                boolean S = this.check(nowNode, nextNodes, completeList, 0, y, 1);
                boolean W = this.check(nowNode, nextNodes, completeList, -1, y, 0);
                
                if (N && E) {
                    this.check(nowNode, nextNodes, completeList, 1, y, -1);
                }
                if (E && S) {
                    this.check(nowNode, nextNodes, completeList, 1, y, 1);
                }
                if (W && S) {
                    this.check(nowNode, nextNodes, completeList, -1, y, 1);
                }
                if (W && N) {
                    this.check(nowNode, nextNodes, completeList, -1, y, -1);
                }
            }
    
            if (nextNodes.isEmpty()) {
                continue;
            }
            
            needChecks.addAll(nextNodes);
            needChecks.sort((o1, o2) -> {
                double d1 = o1.getVector3().distance(this.getEnd());
                double d2 = o2.getVector3().distance(this.getEnd());
                if (d1 == d2) {
                    return 0;
                }
                return d1 > d2 ? 1 : -1;
            });
        }
        
        this.processingComplete = true;
    }
    
    private boolean check(Node nowNode, LinkedList<Node> nextNodes, ArrayList<Vector3> completeList, int x, int y, int z) {
        Node nextNode = new Node(nowNode.getVector3().add(x, y, z), nowNode);
        if (completeList.contains(nextNode.getVector3())) {
            return false;
        }
        completeList.add(nextNode.getVector3());
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
        if (!this.getBlock(target.getVector3()).canPassThrough() ||
                !this.getBlock(target.getVector3().add(0, 1, 0)).canPassThrough() ||
                this.getBlock(target.getVector3().add(0, -1, 0)).canPassThrough()) {
            return false;
        }
        
        //跳跃检查
        if (target.getVector3().getY() > nowNode.getVector3().getY() &&
                !this.getBlock(nowNode.getVector3().add(0, 2, 0)).canPassThrough()) {
            return false;
        }
        
        if (target.getVector3().getY() < nowNode.getVector3().getY() &&
                !this.getBlock(target.getVector3().add(0, 2, 0)).canPassThrough()) {
            return false;
        }
        
        return true;
    }
    
    public void show() {
        for (Node node : nodes) {
            level.addParticleEffect(node.getVector3(), ParticleEffect.REDSTONE_TORCH_DUST);
            level.addParticleEffect(node.getVector3().add(0.1, 0, 0.1), ParticleEffect.REDSTONE_TORCH_DUST);
            level.addParticleEffect(node.getVector3().add(0.1, 0, -0.1), ParticleEffect.REDSTONE_TORCH_DUST);
            level.addParticleEffect(node.getVector3().add(-0.1, 0, 0.1), ParticleEffect.REDSTONE_TORCH_DUST);
            level.addParticleEffect(node.getVector3().add(-0.1, 0, -0.1), ParticleEffect.REDSTONE_TORCH_DUST);
        }
    }
    
    public Block getBlock(Node node) {
        return this.getBlock(node.getVector3());
    }
    
    public Block getBlock(Vector3 vector3) {
        return this.getBlock(vector3.getFloorX(), vector3.getFloorY(), vector3.getFloorZ());
    }
    
    public Block getBlock(int x, int y, int z) {
        int fullState;
        if (y >= 0 && y < 256) {
            int cx = x >> 4;
            int cz = z >> 4;
            BaseFullChunk chunk = this.getLevel().getChunk(cx, cz);
            
            if (chunk != null) {
                fullState = chunk.getFullBlock(x & 15, y, z & 15);
            } else {
                fullState = 0;
            }
        } else {
            fullState = 0;
        }
        
        Block block = Block.fullList[fullState & 4095].clone();
        block.x = x;
        block.y = y;
        block.z = z;
        block.level = this.getLevel();
        return block;
    }
    
}
