/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.player;

import io.gomint.GoMint;
import io.gomint.entity.Entity;
import io.gomint.math.MathUtils;
import io.gomint.server.GoMintServer;
import io.gomint.server.entity.EntityLiving;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.entity.potion.effect.Effect;
import io.gomint.server.network.packet.PacketMobEffect;
import io.gomint.server.util.Values;
import io.gomint.taglib.NBTTagCompound;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.bytes.ByteOpenHashSet;
import it.unimi.dsi.fastutil.bytes.ByteSet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
public class EffectManager {

    private final EntityLiving<?> living;
    private final Byte2ObjectMap<Effect> effects = new Byte2ObjectOpenHashMap<>();

    public EffectManager(EntityLiving<?> living) {
        this.living = living;
    }

    /**
     * Update effects (look if we can remove some)
     *
     * @param currentTimeMillis time when the tick started
     * @param dT                difference time for a full second
     */
    public void update( long currentTimeMillis, float dT ) {
        if ( !this.effects.isEmpty() ) {
            ByteSet removeEffects = null;

            for ( Byte2ObjectMap.Entry<Effect> entry : this.effects.byte2ObjectEntrySet() ) {
                if ( currentTimeMillis >= entry.getValue().getRunoutTimer() ) {
                    if ( removeEffects == null ) {
                        removeEffects = new ByteOpenHashSet();
                    }

                    removeEffects.add( entry.getByteKey() );
                } else {
                    entry.getValue().update( currentTimeMillis, dT );
                }
            }

            if ( removeEffects != null ) {
                for ( Byte removeEffect : removeEffects ) {
                    removeEffect( removeEffect );
                }
            }
        }
    }

    /**
     * Add or modify the effect
     *
     * @param id     of the effect
     * @param effect which should be added or modified
     */
    public void addEffect( byte id, Effect effect ) {
        // Check if we have a old effect
        Effect old = this.effects.get( id );
        if ( old != null ) {
            old.remove( this.living );

            sendPacket( PacketMobEffect.EVENT_MODIFY, id, effect.getAmplifier(), effect.isVisible(),
                MathUtils.fastFloor( ( effect.getRunoutTimer() - this.living.world().currentTickTime() ) / Values.CLIENT_TICK_MS ) );
        } else {
            sendPacket( PacketMobEffect.EVENT_ADD, id, effect.getAmplifier(),
                effect.isVisible(), MathUtils.fastFloor( ( effect.getRunoutTimer() - this.living.world().currentTickTime() ) / Values.CLIENT_TICK_MS ) );
        }

        effect.apply( this.living );
        this.effects.put( id, effect );
    }

    /**
     * Remove effect
     *
     * @param id of the effect
     */
    public void removeEffect( byte id ) {
        // Remove effect
        Effect old = this.effects.remove( id );
        if ( old != null ) {
            old.remove( this.living );
            sendPacket( PacketMobEffect.EVENT_REMOVE, id, 0, false, 0 );
        }
    }

    /**
     * Remove all active effects
     */
    public void removeAll() {
        ByteSet removeEffects = null;

        for ( Byte2ObjectMap.Entry<Effect> entry : this.effects.byte2ObjectEntrySet() ) {
            if ( removeEffects == null ) {
                removeEffects = new ByteOpenHashSet();
            }

            removeEffects.add( entry.getByteKey() );
        }

        if ( removeEffects != null ) {
            for ( Byte removeEffect : removeEffects ) {
                removeEffect( removeEffect );
            }
        }
    }

    private void sendPacket( byte mode, byte id, int amplifier, boolean visible, int duration ) {
        PacketMobEffect mobEffect = new PacketMobEffect();
        mobEffect.setEntityId( this.living.id() );
        mobEffect.setAction( mode );
        mobEffect.setEffectId( id );
        mobEffect.setAmplifier( amplifier );
        mobEffect.setVisible( visible );
        mobEffect.setDuration( duration );

        if ( this.living instanceof EntityPlayer ) {
            ( (EntityPlayer) this.living ).connection().addToSendQueue( mobEffect );
        }

        for ( Entity<?> entity : this.living.getAttachedEntities() ) {
            if ( entity instanceof EntityPlayer ) {
                ( (EntityPlayer) entity ).connection().addToSendQueue( mobEffect );
            }
        }
    }

    public boolean hasEffect( byte effectId ) {
        return this.effects.get( effectId ) != null;
    }

    public int getEffectAmplifier( byte effectId ) {
        Effect effect = this.effects.get( effectId );
        return ( effect == null ) ? -1 : effect.getAmplifier();
    }

    public void sendForPlayer( EntityPlayer player ) {
        for ( Byte2ObjectMap.Entry<Effect> entry : this.effects.byte2ObjectEntrySet() ) {
            if ( entry.getValue().isVisible() ) {
                PacketMobEffect mobEffect = new PacketMobEffect();
                mobEffect.setEntityId( this.living.id() );
                mobEffect.setAction( PacketMobEffect.EVENT_ADD );
                mobEffect.setEffectId( entry.getByteKey() );
                mobEffect.setAmplifier( entry.getValue().getAmplifier() );
                mobEffect.setVisible( entry.getValue().isVisible() );
                mobEffect.setDuration( MathUtils.fastFloor( ( entry.getValue().getRunoutTimer() - this.living.world().currentTickTime() ) / Values.CLIENT_TICK_MS ) );
                player.connection().addToSendQueue( mobEffect );
            }
        }
    }

    public void updateEffect( Effect effect ) {
        sendPacket( PacketMobEffect.EVENT_MODIFY, effect.getId(), effect.getAmplifier(), effect.isVisible(),
            MathUtils.fastFloor( ( effect.getRunoutTimer() - this.living.world().currentTickTime() ) / Values.CLIENT_TICK_MS ) );
    }

    public boolean hasActiveEffect() {
        return !this.effects.isEmpty();
    }

    public void persistToNBT( NBTTagCompound compound ) {
        List<NBTTagCompound> nbtEffects = new ArrayList<>();
        for ( Byte2ObjectMap.Entry<Effect> entry : this.effects.byte2ObjectEntrySet() ) {
            NBTTagCompound effect = new NBTTagCompound( "" );
            effect.addValue( "Amplifier", (byte) entry.getValue().getAmplifier() );
            effect.addValue( "Duration", (int) ( entry.getValue().getRunoutTimer() - this.living.world().currentTickTime() ) / Values.CLIENT_TICK_MS );
            effect.addValue( "Id", entry.getByteKey() );
            effect.addValue( "ShowParticles", (byte) ( entry.getValue().isVisible() ? 1 : 0 ) );
            nbtEffects.add( effect );
        }

        compound.addValue( "ActiveEffects", nbtEffects );
    }

    public void initFromNBT( NBTTagCompound compound ) {
        List<Object> nbtEffects = compound.getList( "ActiveEffects", false );
        if ( nbtEffects != null ) {
            for ( Object nbtEffect : nbtEffects ) {
                NBTTagCompound effect = (NBTTagCompound) nbtEffect;

                byte effectId = effect.getByte( "Id", (byte) -1 );
                if ( effectId > -1 ) {
                    Effect effectInstance = ((GoMintServer) GoMint.instance()).effects().generate( effectId, effect.getByte( "Amplifier", (byte) 0 ),
                        effect.getInteger( "Duration", 1 ) * (int) Values.CLIENT_TICK_MS, this );

                    if ( effect.getByte( "ShowParticles", (byte) 1 ) == 0 ) {
                        effectInstance.visible( false );
                    }

                    this.addEffect( effectId, effectInstance );
                }
            }
        }
    }

}
