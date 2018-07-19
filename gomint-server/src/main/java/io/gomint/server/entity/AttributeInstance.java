package io.gomint.server.entity;

import io.gomint.math.MathUtils;
import io.gomint.taglib.NBTTagCompound;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author geNAZt
 * @version 1.0
 */
@ToString
@Getter
public class AttributeInstance {

    private static final Logger LOGGER = LoggerFactory.getLogger( AttributeInstance.class );

    private final String key;
    private float minValue;
    private float maxValue;
    private float defaultValue;
    private float value;
    private boolean dirty;

    private Map<AttributeModifier, Float> modifiers = new EnumMap<>( AttributeModifier.class );
    private Map<AttributeModifier, Float> multiplyModifiers = new EnumMap<>( AttributeModifier.class );

    AttributeInstance( String key, float minValue, float maxValue, float value ) {
        this.key = key;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.value = value;
        this.defaultValue = value;
        this.dirty = true;
    }

    public void setModifier( AttributeModifier modifier, float amount ) {
        this.modifiers.put( modifier, amount );
        this.recalc();
    }

    private void recalc() {
        this.value = this.defaultValue;

        // Add normal modifierts
        for ( Float aFloat : this.modifiers.values() ) {
            this.value += aFloat;
        }

        // Now add multiply modifiers
        for ( Float aFloat : this.multiplyModifiers.values() ) {
            this.value += this.defaultValue * aFloat;
        }

        // Clamp
        this.value = MathUtils.clamp( this.value, this.minValue, this.maxValue );
        this.dirty = true;
    }

    public void removeModifier( AttributeModifier modifier ) {
        Float amount = this.modifiers.remove( modifier );
        if ( amount != null ) {
            this.recalc();
        }
    }

    public void setValue( float value ) {
        if ( value < this.minValue || value > this.maxValue ) {
            throw new IllegalArgumentException( "Value is not withing bounds: " + value + "; max: " + this.maxValue + "; min: " + this.minValue );
        }

        this.value = value;
        this.dirty = true;
    }

    public boolean isDirty() {
        boolean val = this.dirty;
        this.dirty = false;
        return val;
    }

    public void reset() {
        this.modifiers.clear();
        this.value = this.defaultValue;
        this.dirty = true;
    }

    public void setMaxValue( float maxValue ) {
        this.maxValue = maxValue;
    }

    public void setMultiplyModifier( AttributeModifier modifier, float amount ) {
        this.multiplyModifiers.put( modifier, amount );
        this.recalc();
    }

    public void removeMultiplyModifier( AttributeModifier modifier ) {
        Float amount = this.multiplyModifiers.remove( modifier );
        if ( amount != null ) {
            this.recalc();
        }
    }

    public void initFromNBT( NBTTagCompound compound ) {
        this.defaultValue = compound.getFloat( "Base", this.defaultValue );

        List<Object> nbtAmplifiers = compound.getList( "Modifiers", false );
        if ( nbtAmplifiers != null ) {
            for ( Object amplifier: nbtAmplifiers ) {
                NBTTagCompound nbtAmplifier = (NBTTagCompound) amplifier;

                AttributeModifier modifier = null;
                try {
                    modifier = AttributeModifier.valueOf( nbtAmplifier.getString( "Name", "" ) );
                } catch ( Exception e ) {
                    // Ignored (modified will be null when not known)
                }

                int operation = nbtAmplifier.getInteger( "Operation", 0 );
                float amount = nbtAmplifier.getFloat( "Amount", 0f );

                if ( modifier != null && amount != 0 ) {
                    switch ( operation ) {
                        case 0:
                            this.modifiers.put( modifier, amount );
                            break;
                        case 1:
                            this.multiplyModifiers.put( modifier, amount );
                            break;
                        default:
                            break;
                    }

                    this.recalc();
                }
            }
        }
    }

    public NBTTagCompound persistToNBT() {
        NBTTagCompound compound = new NBTTagCompound( "" );
        compound.addValue( "Name", this.key );
        compound.addValue( "Base", (double) this.defaultValue );

        // Check for 0 mode multipliers (simple addition)
        List<NBTTagCompound> nbtModifiers = new ArrayList<>();
        if ( !this.modifiers.isEmpty() ) {
            for ( Map.Entry<AttributeModifier, Float> entry: this.modifiers.entrySet() ) {
                NBTTagCompound nbtTagCompound = new NBTTagCompound( "" );
                nbtTagCompound.addValue( "Name", entry.getKey().name() );
                nbtTagCompound.addValue( "Operation", 0 );
                nbtTagCompound.addValue( "Amount", (double) entry.getValue() );
                nbtModifiers.add( nbtTagCompound );
            }
        }

        compound.addValue( "Modifiers", nbtModifiers );
        return compound;
    }

}
