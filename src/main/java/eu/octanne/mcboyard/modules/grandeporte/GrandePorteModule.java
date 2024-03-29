package eu.octanne.mcboyard.modules.grandeporte;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import eu.octanne.mcboyard.modules.PlugModule;

public class GrandePorteModule extends PlugModule implements CommandExecutor, TabCompleter {
    enum PORTES {
        // Format : PORTE_FACE_ETAGE
        PORTE_OUEST_0,
        PORTE_NORD_1,
        PORTE_SUD_0,
    }

    private EnumMap<PORTES, GrandePorte> portes = new EnumMap<>(PORTES.class);

    public GrandePorteModule(JavaPlugin instance) {
        super(instance);
    }

    @Override
    public void onEnable() {
        pl.getCommand("grandeporte").setExecutor(this);
        pl.getCommand("grandeporte").setTabCompleter(this);
    }

    @Override
    public void onDisable() {
        for (GrandePorte porte : portes.values()) {
            porte.stopAnimation();
        }
        portes.clear();
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("mcboyard"))
            return null;
        if (args.length == 1) {
            return Arrays.asList("replace", "open", "close", "toggle", "rotate");
        }
        if (args.length == 2) {
            return Stream.of(PORTES.values()).map(Enum::name).toList();
        }
        if (args.length == 3) {
            if (args[0].equals("rotate")) {
                return Arrays.asList("0", "50", "90", "130");
            }
        }

        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        switch (args.length > 0 ? args[0] : "") {
            case "replace":
                return onReplaceCommand(sender, args);
            case "open":
                return onOpenCommand(sender, args);
            case "close":
                return onCloseCommand(sender, args);
            case "toggle":
                return onToggleCommand(sender, args);
            case "rotate":
                return onRotateCommand(sender, args);
            default:
                sender.sendMessage("§cUsage: /grandeporte <replace|open|close|toggle>");
                return false;
        }
    }

    public GrandePorte getPorte(PORTES porte) {
        if (!portes.containsKey(porte)) {
            portes.put(porte, new GrandePorte(porte));
        }
        return portes.get(porte);
    }

    private GrandePorte getPorte(String porte) {
        if (porte == null || porte.isEmpty())
            return null;
        try {
            return getPorte(PORTES.valueOf(porte));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean onReplaceCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        GrandePorte porte = args.length > 1 ? getPorte(args[1]) : null;
        if (porte == null) {
            for (PORTES p : PORTES.values()) {
                getPorte(p).replace();
            }
            sender.sendMessage("Portes réinitialisées");
        } else {
            porte.replace();
            sender.sendMessage("Porte réinitialisée");
        }
        return true;
    }

    private boolean onOpenCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        GrandePorte porte = args.length > 1 ? getPorte(args[1]) : null;
        if (porte == null) {
            sender.sendMessage("§cUsage: /grandeporte open <porte>");
            return false;
        } else {
            if (porte.isOpen() && !porte.isAnimationRunning()) {
                sender.sendMessage("§cLa porte est déjà ouverte");
                return false;
            }
            porte.open();
            sender.sendMessage("La porte s'ouvre");
            return true;
        }
    }

    private boolean onCloseCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        GrandePorte porte = args.length > 1 ? getPorte(args[1]) : null;
        if (porte == null) {
            sender.sendMessage("§cUsage: /grandeporte close <porte>");
            return false;
        } else {
            if (!porte.isOpen() && !porte.isAnimationRunning()) {
                sender.sendMessage("§cLa porte est déjà fermée");
                return false;
            }
            porte.close();
            sender.sendMessage("La porte se ferme");
            return true;
        }
    }

    private boolean onToggleCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        GrandePorte porte = args.length > 1 ? getPorte(args[1]) : null;
        if (porte == null) {
            sender.sendMessage("§cUsage: /grandeporte toggle <porte>");
            return false;
        } else {
            porte.toggle();
            if (porte.isOpen()) {
                sender.sendMessage("La porte s'ouvre");
            } else {
                sender.sendMessage("La porte se ferme");
            }
            return true;
        }
    }

    private boolean onRotateCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        GrandePorte porte = args.length > 1 ? getPorte(args[1]) : null;
        if (porte == null) {
            sender.sendMessage("§cUsage: /grandeporte rotate <porte> <angle>");
            return false;
        }
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /grandeporte rotate <porte> <angle> (angle2)");
            return false;
        }

        float angle1;
        float angle2;
        try {
            angle1 = Integer.parseInt(args[2]);
            angle2 = args.length > 3 ? Integer.parseInt(args[3]) : angle1;
        } catch (NumberFormatException e) {
            sender.sendMessage("§cL'angle doit être un nombre");
            return false;
        }

        porte.setRotation(angle1, angle2);
        sender.sendMessage("L'ouverture de la porte a changé");
        return true;
    }
}