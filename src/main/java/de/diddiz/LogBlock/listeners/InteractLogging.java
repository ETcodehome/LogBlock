package de.diddiz.LogBlock.listeners;

import de.diddiz.LogBlock.Actor;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.Logging;
import de.diddiz.LogBlock.config.WorldConfig;
import de.diddiz.util.BukkitUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Cake;
import org.bukkit.block.data.type.Comparator;
import org.bukkit.block.data.type.Comparator.Mode;
import org.bukkit.block.data.type.DaylightDetector;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.block.data.type.Repeater;
import org.bukkit.block.data.type.Switch;
import org.bukkit.block.data.type.TurtleEgg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import static de.diddiz.LogBlock.config.Config.getWorldConfig;

public class InteractLogging extends LoggingListener {
    public InteractLogging(LogBlock lb) {
        super(lb);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        final WorldConfig wcfg = getWorldConfig(event.getPlayer().getWorld());
        if (wcfg != null) {
            final Block clicked = event.getClickedBlock();
            if (clicked == null) {
                return;
            }
            final BlockData blockData = clicked.getBlockData();
            final Material type = blockData.getMaterial();
            final Player player = event.getPlayer();
            final Location loc = clicked.getLocation();

            switch (type) {
                case OAK_FENCE_GATE:
                case SPRUCE_FENCE_GATE:
                case BIRCH_FENCE_GATE:
                case JUNGLE_FENCE_GATE:
                case ACACIA_FENCE_GATE:
                case DARK_OAK_FENCE_GATE:
                case OAK_TRAPDOOR:
                case SPRUCE_TRAPDOOR:
                case BIRCH_TRAPDOOR:
                case JUNGLE_TRAPDOOR:
                case ACACIA_TRAPDOOR:
                case DARK_OAK_TRAPDOOR:
                    if (wcfg.isLogging(Logging.DOORINTERACT) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        Openable newBlockData = (Openable) blockData.clone();
                        newBlockData.setOpen(!newBlockData.isOpen());
                        consumer.queueBlock(Actor.actorFromEntity(player), loc, blockData, newBlockData);
                    }
                    break;
                case CAKE:
                    if (wcfg.isLogging(Logging.CAKEEAT) && event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getFoodLevel() < 20) {
                        Cake newBlockData = (Cake) blockData.clone();
                        if (newBlockData.getBites() < 6) {
                            newBlockData.setBites(newBlockData.getBites() + 1);
                            consumer.queueBlock(Actor.actorFromEntity(player), loc, blockData, newBlockData);
                        } else {
                            consumer.queueBlock(Actor.actorFromEntity(player), loc, blockData, Material.AIR.createBlockData());
                        }
                    }
                    break;
                case NOTE_BLOCK:
                    if (wcfg.isLogging(Logging.NOTEBLOCKINTERACT) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        NoteBlock newBlockData = (NoteBlock) blockData.clone();
                        if (newBlockData.getNote().getOctave() == 2) {
                            newBlockData.setNote(new Note(0, Tone.F, true));
                        } else {
                            newBlockData.setNote(newBlockData.getNote().sharped());
                        }
                        consumer.queueBlock(Actor.actorFromEntity(player), loc, blockData, newBlockData);
                    }
                    break;
                case REPEATER:
                    if (wcfg.isLogging(Logging.DIODEINTERACT) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        Repeater newBlockData = (Repeater) blockData.clone();
                        newBlockData.setDelay((newBlockData.getDelay() % 4) + 1);
                        consumer.queueBlock(Actor.actorFromEntity(player), loc, blockData, newBlockData);
                    }
                    break;
                case COMPARATOR:
                    if (wcfg.isLogging(Logging.COMPARATORINTERACT) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        Comparator newBlockData = (Comparator) blockData.clone();
                        newBlockData.setMode(newBlockData.getMode() == Mode.COMPARE ? Mode.SUBTRACT : Mode.COMPARE);
                        consumer.queueBlock(Actor.actorFromEntity(player), loc, blockData, newBlockData);
                    }
                    break;
                case DAYLIGHT_DETECTOR:
                    if (wcfg.isLogging(Logging.DAYLIGHTDETECTORINTERACT) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        DaylightDetector newBlockData = (DaylightDetector) blockData.clone();
                        newBlockData.setInverted(!newBlockData.isInverted());
                        consumer.queueBlock(Actor.actorFromEntity(player), loc, blockData, newBlockData);
                    }
                    break;
                case OAK_PRESSURE_PLATE:
                case SPRUCE_PRESSURE_PLATE:
                case BIRCH_PRESSURE_PLATE:
                case JUNGLE_PRESSURE_PLATE:
                case ACACIA_PRESSURE_PLATE:
                case DARK_OAK_PRESSURE_PLATE:
                case STONE_PRESSURE_PLATE:
                case HEAVY_WEIGHTED_PRESSURE_PLATE:
                case LIGHT_WEIGHTED_PRESSURE_PLATE:
                    if (wcfg.isLogging(Logging.PRESUREPLATEINTERACT) && event.getAction() == Action.PHYSICAL) {
                        consumer.queueBlock(Actor.actorFromEntity(player), loc, blockData, blockData);
                    }
                    break;
                case TRIPWIRE:
                    if (wcfg.isLogging(Logging.TRIPWIREINTERACT) && event.getAction() == Action.PHYSICAL) {
                        consumer.queueBlock(Actor.actorFromEntity(player), loc, blockData, blockData);
                    }
                    break;
                case FARMLAND:
                    if (wcfg.isLogging(Logging.CROPTRAMPLE) && event.getAction() == Action.PHYSICAL) {
                        // 3 = Dirt ID
                        consumer.queueBlock(Actor.actorFromEntity(player), loc, blockData, Material.DIRT.createBlockData());
                        // Log the crop on top as being broken
                        Block trampledCrop = clicked.getRelative(BlockFace.UP);
                        if (BukkitUtils.getCropBlocks().contains(trampledCrop.getType())) {
                            consumer.queueBlockBreak(Actor.actorFromEntity(player), trampledCrop.getState());
                        }
                    }
                    break;
                case TURTLE_EGG:
                    if (event.getAction() == Action.PHYSICAL) {
                        TurtleEgg turtleEggData = (TurtleEgg) blockData;
                        int eggs = turtleEggData.getEggs();
                        if (eggs > 1) {
                            TurtleEgg turtleEggData2 = (TurtleEgg) turtleEggData.clone();
                            turtleEggData2.setEggs(eggs - 1);
                            consumer.queueBlock(Actor.actorFromEntity(player), loc, turtleEggData, turtleEggData2);
                        } else {
                            consumer.queueBlock(Actor.actorFromEntity(player), loc, turtleEggData, Material.AIR.createBlockData());
                        }
                    }
                    break;
                default:
                    if (BukkitUtils.isButton(type) || type == Material.LEVER) {
                        if (wcfg.isLogging(Logging.SWITCHINTERACT) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            Switch newBlockData = (Switch) blockData.clone();
                            if (!newBlockData.isPowered() || type == Material.LEVER) {
                                newBlockData.setPowered(!newBlockData.isPowered());
                            }
                            consumer.queueBlock(Actor.actorFromEntity(player), loc, blockData, newBlockData);
                        }
                    }
                    if (BukkitUtils.isWoodenDoor(type)) {
                        if (wcfg.isLogging(Logging.DOORINTERACT) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            Door newBlockData = (Door) blockData.clone();
                            newBlockData.setOpen(!newBlockData.isOpen());
                            consumer.queueBlock(Actor.actorFromEntity(player), loc, blockData, newBlockData);
                        }
                    }
            }
        }
    }
}
