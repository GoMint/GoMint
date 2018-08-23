package io.gomint.server.command.internal;

import io.gomint.command.Command;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.PlayerCommandSender;
import io.gomint.command.annotation.*;
import io.gomint.command.validator.BlockPositionValidator;
import io.gomint.command.validator.StringValidator;
import io.gomint.entity.monster.*;
import io.gomint.entity.passive.*;
import io.gomint.math.BlockPosition;
import io.gomint.math.Location;
import io.gomint.server.entity.EntityPlayer;

import java.util.Map;

/**
 * @author Kaooot
 * @version 1.0
 */
@Name( "summon" )
@Description( "Summons an entity." )
@Permission( "gomint.command.summon" )
@Overload( {
    @Parameter( name = "entityType", validator = StringValidator.class, arguments = {".*"} ),
    @Parameter( name = "position", validator = BlockPositionValidator.class, optional = true )
} )
public class SummonCommand extends Command {

    @Override
    public CommandOutput execute( CommandSender commandSender, String alias, Map<String, Object> arguments ) {
        CommandOutput output = new CommandOutput();

        String entityType = (String) arguments.get( "entityType" );
        BlockPosition position = (BlockPosition) arguments.get( "position" );

        if( commandSender instanceof PlayerCommandSender ) {
            EntityPlayer player = (EntityPlayer) commandSender;

            if( String.valueOf( position.getX() ).equals( "~" ) ) {
                position.setX( player.getLocation().toBlockPosition().getX() );
            }
            if( String.valueOf( position.getY() ).equals( "~" ) ) {
                position.setY( player.getLocation().toBlockPosition().getY() );
            }
            if( String.valueOf( position.getZ() ).equals( "~" ) ) {
                position.setZ( player.getLocation().toBlockPosition().getZ() );
            }
            if( position.toString().isEmpty() || position.toString().equals( "" ) ) {
                position.setX( player.getLocation().toBlockPosition().getX() );
                position.setY( player.getLocation().toBlockPosition().getY() );
                position.setZ( player.getLocation().toBlockPosition().getZ() );
            }

            if( entityType.equals( "" ) ) {
                output.fail( "§cUsage: /summon <entityType> <position>" );
            }

            Location location = player.getLocation();
            location.setX( position.getX() );
            location.setY( position.getY() );
            location.setZ( position.getZ() );

            switch( entityType ) {
                case "bat":
                    EntityBat bat = EntityBat.create();
                    bat.spawn( location );
                    break;
                case "chicken":
                    EntityChicken chicken = EntityChicken.create();
                    chicken.spawn( location );
                    break;
                case "cod":
                    EntityCod cod = EntityCod.create();
                    cod.spawn( location );
                    break;
                case "cow":
                    EntityCow cow = EntityCow.create();
                    cow.spawn( location );
                    break;
                case "donkey":
                    EntityDonkey donkey = EntityDonkey.create();
                    donkey.spawn( location );
                    break;
                case "horse":
                    EntityHorse horse = EntityHorse.create();
                    horse.spawn( location );
                    break;
                case "mooshroom":
                    EntityMooshroom mooshroom = EntityMooshroom.create();
                    mooshroom.spawn( location );
                    break;
                case "mule":
                    EntityMule mule = EntityMule.create();
                    mule.spawn( location );
                    break;
                case "ocelot":
                    EntityOcelot ocelot = EntityOcelot.create();
                    ocelot.spawn( location );
                    break;
                case "parrot":
                    EntityParrot parrot = EntityParrot.create();
                    parrot.spawn( location );
                    break;
                case "pig":
                    EntityPig pig = EntityPig.create();
                    pig.spawn( location );
                    break;
                case "pufferfish":
                    EntityPufferfish pufferfish = EntityPufferfish.create();
                    pufferfish.spawn( location );
                    break;
                case "rabbit":
                    EntityRabbit rabbit = EntityRabbit.create();
                    rabbit.spawn( location );
                    break;
                case "sheep":
                    EntitySheep sheep = EntitySheep.create();
                    sheep.spawn( location );
                    break;
                case "skeleton_horse":
                    EntitySkeletonHorse skeletonHorse = EntitySkeletonHorse.create();
                    skeletonHorse.spawn( location );
                    break;
                case "salmon":
                    EntitySalmon salmon = EntitySalmon.create();
                    salmon.spawn( location );
                    break;
                case "squid":
                    EntitySquid squid = EntitySquid.create();
                    squid.spawn( location );
                    break;
                case "turtle":
                    EntityTurtle turtle = EntityTurtle.create();
                    turtle.spawn( location );
                    break;
                case "tropicalfish":
                    EntityTropicalFish tropicalFish = EntityTropicalFish.create();
                    tropicalFish.spawn( location );
                    break;
                case "villager":
                    EntityVillager villager = EntityVillager.create();
                    villager.spawn( location );
                    break;
                case "llama":
                    EntityLama lama = EntityLama.create();
                    lama.spawn( location );
                    break;
                case "polar_bear":
                    EntityPolarBear polarBear = EntityPolarBear.create();
                    polarBear.spawn( location );
                    break;
                case "wolf":
                    EntityWolf wolf = EntityWolf.create();
                    wolf.spawn( location );
                    break;
                case "cave_spider":
                    EntityCaveSpider caveSpider = EntityCaveSpider.create();
                    caveSpider.spawn( location );
                    break;
                case "enderman":
                    EntityEnderman enderman = EntityEnderman.create();
                    enderman.spawn( location );
                    break;
                case "spider":
                    EntitySpider spider = EntitySpider.create();
                    spider.spawn( location );
                    break;
                case "zombie_pigman":
                    EntityZombiePigman zombiePigman = EntityZombiePigman.create();
                    zombiePigman.spawn( location );
                    break;
                case "blaze":
                    EntityBlaze blaze = EntityBlaze.create();
                    blaze.spawn( location );
                    break;
                case "creeper":
                    EntityCreeper creeper = EntityCreeper.create();
                    creeper.spawn( location );
                    break;
                case "drowned":
                    EntityDrowned drowned = EntityDrowned.create();
                    drowned.spawn( location );
                    break;
                case "elder_guardian":
                    EntityElderGuardian elderGuardian = EntityElderGuardian.create();
                    elderGuardian.spawn( location );
                    break;
                case "endermite":
                    EntityEndermite endermite = EntityEndermite.create();
                    endermite.spawn( location );
                    break;
                case "evocation_illager":
                    EntityEvoker evoker = EntityEvoker.create();
                    evoker.spawn( location );
                    break;
                case "ghast":
                    EntityGhast ghast = EntityGhast.create();
                    ghast.spawn( location );
                    break;
                case "guardian":
                    EntityGuardian guardian = EntityGuardian.create();
                    guardian.spawn( location );
                    break;
                case "husk":
                    EntityHusk husk = EntityHusk.create();
                    husk.spawn( location );
                    break;
                case "magma_cube":
                    EntityMagmaCube magmaCube = EntityMagmaCube.create();
                    magmaCube.spawn( location );
                    break;
                case "shulker":
                    EntityShulker shulker = EntityShulker.create();
                    shulker.spawn( location );
                    break;
                case "silverfish":
                    EntitySilverfish silverfish = EntitySilverfish.create();
                    silverfish.spawn( location );
                    break;
                case "skeleton":
                    EntitySkeleton skeleton = EntitySkeleton.create();
                    skeleton.spawn( location );
                    break;
                case "slime":
                    EntitySlime slime = EntitySlime.create();
                    slime.spawn( location );
                    break;
                case "stray":
                    EntityStray stray = EntityStray.create();
                    stray.spawn( location );
                    break;
                case "vex":
                    EntityVex vex = EntityVex.create();
                    vex.spawn( location );
                    break;
                case "vindicator":
                    EntityVindicator vindicator = EntityVindicator.create();
                    vindicator.spawn( location );
                    break;
                case "witch":
                    EntityWitch witch = EntityWitch.create();
                    witch.spawn( location );
                    break;
                case "wither_skeleton":
                    EntityWitherSkeleton witherSkeleton = EntityWitherSkeleton.create();
                    witherSkeleton.spawn( location );
                    break;
                case "zombie":
                    EntityZombie zombie = EntityZombie.create();
                    zombie.spawn( location );
                    break;
                case "zombie_horse":
                    EntityZombieHorse zombieHorse = EntityZombieHorse.create();
                    zombieHorse.spawn( location );
                    break;
                case "zombie_villager":
                    EntityZombieVillager zombieVillager = EntityZombieVillager.create();
                    zombieVillager.spawn( location );
                    break;
            }
        }
        return output;
    }
}
