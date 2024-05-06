package com.ultreon.quantum.client.gui.screens.container;

import com.badlogic.gdx.math.MathUtils;
import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.config.ClientConfig;
import com.ultreon.quantum.client.gui.Renderer;
import com.ultreon.quantum.item.ItemStack;
import com.ultreon.quantum.menu.Inventory;
import com.ultreon.quantum.menu.ItemSlot;
import com.ultreon.quantum.network.packets.c2s.C2SCraftRecipePacket;
import com.ultreon.quantum.recipe.CraftingRecipe;
import com.ultreon.quantum.recipe.Recipe;
import com.ultreon.quantum.recipe.RecipeManager;
import com.ultreon.quantum.recipe.RecipeType;
import com.ultreon.quantum.text.TextObject;
import com.ultreon.quantum.util.Color;
import com.ultreon.quantum.util.Identifier;
import com.ultreon.quantum.util.PagedList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class InventoryScreen extends ContainerScreen {
    private static final int CONTAINER_SIZE = 40;
    private static final Identifier BACKGROUND = QuantumClient.id("textures/gui/container/inventory.png");
    private final Inventory inventory;
    private PagedList<? extends CraftingRecipe> recipes;
    private List<? extends CraftingRecipe> currentPage;
    private int page = 0;
    private final List<ItemSlot> recipeSlots = new ArrayList<>();

    public InventoryScreen(Inventory inventory, TextObject title) {
        super(inventory, title, InventoryScreen.CONTAINER_SIZE);
        this.inventory = inventory;

        this.recipes = RecipeManager.get().getRecipes(RecipeType.CRAFTING, 30, inventory);
        this.currentPage = this.recipes.getFullPage(this.page);
        this.rebuildSlots();
    }


    public void nextPage() {
        var page = this.page + 1;
        if (page > MathUtils.ceil(this.recipes.size() / 30f) - 1) {
            page = 0;
        }
        this.page = page;
        this.currentPage = this.recipes.getFullPage(this.page);
        this.rebuildSlots();
    }

    public void previousPage() {
        var page = this.page - 1;
        if (page < 0) {
            page = MathUtils.ceil(this.recipes.size() / 30f) - 1;
        }
        this.page = page;
        this.currentPage = this.recipes.getFullPage(this.page);
        this.rebuildSlots();
    }

    private void rebuildSlots() {
        if (!QuantumClient.isOnMainThread()) {
            QuantumClient.invoke(this::rebuildSlots);
            return;
        }

        this.recipeSlots.clear();
        List<ItemSlot> list = new ArrayList<>();
        int x = 0;
        int y = 0;
        this.recipes = RecipeManager.get().getRecipes(RecipeType.CRAFTING, 30, inventory);
        this.currentPage = this.recipes.getFullPage(this.page);
        for (Recipe recipe : this.currentPage) {
            if (recipe.canCraft(this.inventory)) {
                if (x >= 5) {
                    x = 0;
                    y++;
                }
                ItemSlot itemSlot = this.createItemSlot(recipe, x, y);
                list.add(itemSlot);
                x++;
            }
        }
        this.recipeSlots.addAll(list);
    }

    private ItemSlot createItemSlot(Recipe recipe, int x, int y) {
        return new ItemSlot(-1, this.inventory, recipe.result(),
                this.backgroundWidth() + 7 + x * 19, (int) (this.backgroundHeight() / 2f - 64 + 6 + y * 19));
    }

    @Override
    public int left() {
        return super.left() - 52;
    }

    @Override
    public int backgroundWidth() {
        return 181;
    }

    @Override
    public int backgroundHeight() {
        return 110;
    }

    @Override
    public Identifier getBackground() {
        return InventoryScreen.BACKGROUND;
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);

        Identifier background = this.getBackground();
        renderer.blit(background, this.left() + this.backgroundWidth() + 1, this.getHeight() / 2f - 64, 104, 128, 0, 127);
    }

    @SuppressWarnings("GDXJavaFlushInsideLoop")
    @Override
    protected void renderSlots(Renderer renderer, int mouseX, int mouseY) {
        super.renderSlots(renderer, mouseX, mouseY);

        for (ItemSlot slot : this.recipeSlots) {
            this.renderSlot(renderer, mouseX, mouseY, slot);
        }
    }

    @Override
    public void renderForeground(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.renderForeground(renderer, mouseX, mouseY, deltaTime);

        var slotAt = this.getRecipeSlotAt(mouseX, mouseY);
        if (slotAt != null && !slotAt.slot.getItem().isEmpty()) {
            this.renderTooltip(renderer, mouseX + 4, mouseY + 4, slotAt.slot.getItem().getItem().getTranslation(), this.withRecipeInfo(slotAt.recipe, slotAt.slot.getItem().getDescription()), slotAt.slot.getItem().getItem().getId().toString());
        }
    }

    private List<TextObject> withRecipeInfo(Recipe recipe, List<TextObject> description) {
        var result = new ArrayList<TextObject>();
        var ingredients = recipe.ingredients();
        if (!ingredients.isEmpty()) {
            result.add(TextObject.empty());
            result.add(TextObject.translation("quantum.recipe.ingredients").style(textStyle -> textStyle.color(Color.WHITE).bold(true)));
            for (ItemStack stack : ingredients) {
                result.add(TextObject.literal(stack.getCount() + "x ").append(stack.getItem().getTranslation()));
            }

            if (!this.showOnlyCraftable()) {
                result.add(recipe.canCraft(this.inventory) ? TextObject.translation("quantum.recipe.craftable").style(textStyle -> textStyle.color(Color.GREEN)) : TextObject.translation("quantum.recipe.uncraftable").style(textStyle -> textStyle.color(Color.RED)));
            }

            result.add(TextObject.empty());
        } else {
            result.add(TextObject.translation("quantum.recipe.uncraftable").style(textStyle -> textStyle.color(Color.RED)));
            result.add(TextObject.empty());
        }

        result.addAll(description);

        return result;
    }

    private boolean showOnlyCraftable() {
        return ClientConfig.showOnlyCraftable;
    }

    @Nullable
    private RecipeSlot getRecipeSlotAt(int mouseX, int mouseY) {
        List<ItemSlot> slots = this.recipeSlots;
        List<? extends CraftingRecipe> recipeList = this.currentPage;
        for (int i = 0, slotsSize = slots.size(); i < slotsSize; i++) {
            ItemSlot slot = slots.get(i);
            if (slot.isWithinBounds(mouseX - this.left(), mouseY - this.top())) {
                return new RecipeSlot(recipeList.get(i), slot);
            }
        }
        return null;
    }

    @Override
    public boolean mouseClick(int x, int y, int button, int count) {
        List<ItemSlot> slots = this.recipeSlots;
        for (int i = 0, slotsSize = slots.size(); i < slotsSize; i++) {
            ItemSlot slot = slots.get(i);
            if (slot.isWithinBounds(x - this.left(), y - this.top())) {
                Recipe recipe = this.recipes.get(this.page, i);
                this.client.connection.send(new C2SCraftRecipePacket(recipe.getType(), recipe));
                this.rebuildSlots();
                return true;
            }
        }

        return super.mouseClick(x, y, button, count);
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public void emitUpdate() {
        super.emitUpdate();

        this.rebuildSlots();
    }

    private record RecipeSlot(Recipe recipe, ItemSlot slot) {

    }
}
