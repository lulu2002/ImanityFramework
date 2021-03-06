/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.bukkit.util.items;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.util.LocaleRV;
import org.imanity.framework.bukkit.util.BukkitUtil;
import org.imanity.framework.bukkit.util.nms.NBTEditor;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Getter
@JsonSerialize(using = ImanityItem.Serializer.class)
@JsonDeserialize(using = ImanityItem.Deserializer.class)
public class ImanityItem {

    private static final Int2ObjectMap<ImanityItem> REGISTERED_ITEM = new Int2ObjectOpenHashMap<>();
    private static final AtomicInteger ITEM_COUNTER = new AtomicInteger(0);

    public static ImanityItem getItem(int id) {
        return REGISTERED_ITEM.get(id);
    }

    @Nullable
    public static ImanityItem getItemFromBukkit(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }

        if (!NBTEditor.contains(itemStack, "imanity", "item", "id")) {
            return null;
        }

        int value = NBTEditor.getInt(itemStack, "imanity", "item", "id");

        if (value == -1) {
            return null;
        }

        return REGISTERED_ITEM.get(value);
    }

    private int id;
    private boolean submitted;
    private ItemBuilder itemBuilder;
    private String displayNameLocale;
    private String displayLoreLocale;

    private ItemCallback clickCallback;

    private final List<LocaleRV> displayNamePlaceholders = new ArrayList<>();
    private final List<LocaleRV> displayLorePlaceholders = new ArrayList<>();

    private final Map<String, Object> metadata = new HashMap<>();

    public  Object getMetadata(String key) {
        return this.metadata.get(key);
    }

    public ImanityItem item(ItemBuilder itemBuilder) {
        this.itemBuilder = itemBuilder;
        return this;
    }

    public ImanityItem displayNameLocale(String locale) {
        this.displayNameLocale = locale;
        return this;
    }

    public ImanityItem displayLoreLocale(String locale) {
        this.displayLoreLocale = locale;
        return this;
    }

    public ImanityItem appendNameReplace(String target, Function<Player, String> replacement) {
        this.displayNamePlaceholders.add(LocaleRV.o(target, replacement));
        return this;
    }

    public ImanityItem appendLoreReplace(String target, Function<Player, String> replacement) {
        this.displayLorePlaceholders.add(LocaleRV.o(target, replacement));
        return this;
    }

    public ImanityItem callback(ItemCallback callback) {
        this.clickCallback = callback;
        return this;
    }

    public ImanityItem metadata(String key, Object object) {
        this.metadata.put(key, object);
        return this;
    }

    public ImanityItem submit() {

        this.id = ITEM_COUNTER.getAndIncrement();
        REGISTERED_ITEM.put(this.id, this);

        this.submitted = true;

        return this;

    }

    public Material getType() {
        return this.itemBuilder.getType();
    }

    public ItemStack build(Player receiver) {
        ItemBuilder itemBuilder = this.itemBuilder.clone();

        if (displayNameLocale != null) {
            String name = Imanity.translate(receiver, displayNameLocale);
            for (LocaleRV rv : this.displayNamePlaceholders) {
                name = BukkitUtil.replace(name, rv.getTarget(), rv.getReplacement(receiver));
            }

            itemBuilder.name(name);
        }

        if (displayLoreLocale != null) {
            String lore = Imanity.translate(receiver, displayLoreLocale);
            for (LocaleRV rv : this.displayLorePlaceholders) {
                lore = BukkitUtil.replace(lore, rv.getTarget(), rv.getReplacement(receiver));
            }

            itemBuilder.lore(BukkitUtil.toStringList(lore, "\n"));

        }

        if (!this.submitted) {
            return itemBuilder.build();
        }
        return itemBuilder
                .tag(this.id, "imanity", "item", "id")
                .build();
    }

    public static class Serializer extends StdSerializer<ImanityItem> {

        protected Serializer() {
            super(ImanityItem.class);
        }

        @Override
        public void serialize(ImanityItem item, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeNumber(item.id);
        }
    }

    public static class Deserializer extends StdDeserializer<ImanityItem> {

        protected Deserializer() {
            super(ImanityItem.class);
        }

        @Override
        public ImanityItem deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            return ImanityItem.getItem(jsonParser.getIntValue());
        }
    }
}