package com.artillexstudios.axapi.packet.wrapper.serverbound;

import com.artillexstudios.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axapi.packet.PacketEvent;
import com.artillexstudios.axapi.packet.PacketType;
import com.artillexstudios.axapi.packet.ServerboundPacketTypes;
import com.artillexstudios.axapi.packet.wrapper.PacketWrapper;
import com.artillexstudios.axapi.utils.Vector3d;
import com.artillexstudios.axapi.utils.Vector3f;
import com.artillexstudios.axapi.utils.Version;
import java.util.function.Function;

public final class ServerboundInteractWrapper extends PacketWrapper {
    private int entityId;
    private ActionType type;
    private Action action;
    private boolean usingSecondaryAction;

    public ServerboundInteractWrapper(PacketEvent event) {
        super(event);
    }

    public int entityId() {
        return this.entityId;
    }

    public void entityId(int entityId) {
        this.entityId = entityId;
    }

    public ActionType type() {
        return this.type;
    }

    public void type(ActionType type) {
        this.type = type;
    }

    public Action action() {
        return this.action;
    }

    public void action(Action action) {
        this.action = action;
    }

    public boolean usingSecondaryAction() {
        return this.usingSecondaryAction;
    }

    public void usingSecondaryAction(boolean usingSecondaryAction) {
        this.usingSecondaryAction = usingSecondaryAction;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
        if (Version.getServerVersion().isOlderThan(Version.v26_1)) {
            buf.writeEnum(this.type);
        }
        this.action.write(buf);
        buf.writeBoolean(this.usingSecondaryAction);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v26_1)) {
            this.action = new InteractionAtLocationAction(buf);
        } else {
            this.type = buf.readEnum(ActionType.class);
            this.action = this.type.mapper.apply(buf);
        }
        this.usingSecondaryAction = buf.readBoolean();
    }

    @Override
    public PacketType packetType() {
        return ServerboundPacketTypes.INTERACT;
    }

    public interface Action {
        void write(FriendlyByteBuf buf);
    }

    public enum ActionType {
        INTERACT(InteractionAction::new),
        ATTACK(buf -> new AttackAction()),
        INTERACT_AT(InteractionAtLocationAction::new);

        private final Function<FriendlyByteBuf, Action> mapper;

        ActionType(Function<FriendlyByteBuf, Action> mapper) {
            this.mapper = mapper;
        }
    }

    public static class AttackAction implements Action {
        public AttackAction() {}

        @Override
        public void write(FriendlyByteBuf buf) {}
    }

    public static class InteractionAction implements Action {
        private final InteractionHand hand;

        public InteractionAction(FriendlyByteBuf buf) {
            this.hand = buf.readEnum(InteractionHand.class);
        }

        public InteractionHand hand() {
            return this.hand;
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeEnum(this.hand);
        }
    }

    public static class InteractionAtLocationAction implements Action {
        private final InteractionHand hand;
        private final Vector3d location;

        public InteractionAtLocationAction(FriendlyByteBuf buf) {
            if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v26_1)) {
                this.hand = buf.readEnum(InteractionHand.class);
                this.location = buf.readLpVec3();
            } else {
                this.location = new Vector3d(buf.readVector3f());
                this.hand = buf.readEnum(InteractionHand.class);
            }
        }

        public InteractionHand hand() {
            return this.hand;
        }

        public Vector3d location() {
            return this.location;
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v26_1)) {
                buf.writeEnum(this.hand);
                buf.writeLpVec3(this.location);
            } else {
                buf.writeVector3f(new Vector3f(this.location));
                buf.writeEnum(this.hand);
            }
        }
    }

    public enum InteractionHand {
        MAIN_HAND,
        OFF_HAND
    }
}
