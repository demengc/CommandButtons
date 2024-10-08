/*
 * MIT License
 *
 * Copyright (c) 2023 Demeng Chen
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

package dev.demeng.commandbuttons.util;

import dev.demeng.pluginbase.Common;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

/**
 * General utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utils {

  /**
   * Checks if the provided block is air.
   *
   * @param block The block to check
   * @return True if air, false otherwise
   */
  public static boolean isAir(Block block) {
    return block == null || isAir(block.getType());
  }

  /**
   * Checks if the provided item is air.
   *
   * @param stack The item stack to check
   * @return True if air, false otherwise
   */
  public static boolean isAir(ItemStack stack) {
    return stack == null || isAir(stack.getType());
  }

  private static boolean isAir(Material material) {
    return material == null || material == Material.AIR
        || (Common.isServerVersionAtLeast(13) && material.isAir());
  }
}
