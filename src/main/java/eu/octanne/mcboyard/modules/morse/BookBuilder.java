package eu.octanne.mcboyard.modules.morse;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class BookBuilder {
    private List<Component> pages = new ArrayList<>();
    private StringBuilder builder;
    private int numberOfLinesInCurrentPage;
    private static final int MAX_LINES_PER_PAGE = 14;

    public BookBuilder() {
        this.builder = new StringBuilder();
        this.numberOfLinesInCurrentPage = 0;
    }

    public BookBuilder append(String text) {
        builder.append(text);
        return this;
    }

    public BookBuilder append(char c) {
        builder.append(c);
        return this;
    }

    public BookBuilder endOfLine() {
        builder.append("\n");
        numberOfLinesInCurrentPage++;
        if (numberOfLinesInCurrentPage >= MAX_LINES_PER_PAGE) {
            endOfPage();
        }
        return this;
    }

    public void endOfPage() {
        pages.add(Component.text(builder.toString()));
        builder = new StringBuilder();
        numberOfLinesInCurrentPage = 0;
    }

    public TextComponent[] getPages() {
        if (builder.length() > 0) {
            endOfPage();
        }
        return pages.toArray(new TextComponent[0]);
    }

    public ItemStack toBook(String title, String author) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle(title);
        meta.setAuthor(author);
        meta.pages(getPages());
        book.setItemMeta(meta);
        return book;
    }
}
