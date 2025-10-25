/*
 * MIT License
 *
 * Copyright (c) 2025 Demeng Chen
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
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Serializes and deserializes block locations to/from strings.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocationSerializer {

  private static final String SEPARATOR = ";";

  public static String serialize(Location loc) {
    return Objects.requireNonNull(loc.getWorld(), "World is null").getName()
        + SEPARATOR + loc.getBlockX()
        + SEPARATOR + loc.getBlockY()
        + SEPARATOR + loc.getBlockZ();
  }

  public static Location deserialize(String str) {

    final String[] split = str.split(SEPARATOR);

    if (split.length != 4) {
      throw new IllegalArgumentException("Corrupt location length: " + str);
    }

    final World world = Bukkit.getWorld(split[0]);
    final Integer x = Common.checkInt(split[1]);
    final Integer y = Common.checkInt(split[2]);
    final Integer z = Common.checkInt(split[3]);

    if (world == null || x == null || y == null || z == null) {
      throw new IllegalArgumentException("Corrupt location: " + str);
    }

    return new Location(world, x, y, z);
  }
}
