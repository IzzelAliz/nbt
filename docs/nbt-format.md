# NBT Format Introduction

NBT (Named Binary Tag) format, which Mojang has used for a long time to load and save Minecraft game data, does not have a formal specification.

However, the community has formed several conventional standards related to NBT. Following is a brief introduction to NBT formats.

## NBT types

There are 13 different NBT types recognized by Mojang's system. Each NBT type has its unique number id:

| Name             | Number Id | Description                                                                                                               |
| ---------------- | --------- | ------------------------------------------------------------------------------------------------------------------------- |
| `TAG_End`        | 0         | Singleton. Not really appear in actual uses. Usually used as a fallback when the corresponding value does not exist.      |
| `TAG_Byte`       | 1         | An 8-bit byte from -128 to 127 (corresponding to `byte` type in Java).                                                    |
| `TAG_Short`      | 2         | A 16-bit integer from -32768 to 32767 (corresponding to `short` type in Java).                                            |
| `TAG_Int`        | 3         | A 32-bit integer from -2147483648 to 2147483647 (corresponding to `int` type in Java).                                    |
| `TAG_Long`       | 4         | A 64-bit integer from -9223372036854775808 to 9223372036854775807 (corresponding to `long` type in Java).                 |
| `TAG_Float`      | 5         | A 32-bit single-precision floating-point number in IEEE 754 standard (corresponding to `float` type in Java).             |
| `TAG_Double`     | 6         | A 64-bit double-precision floating-point number in IEEE 754 standard (corresponding to `double` type in Java).            |
| `TAG_Byte_Array` | 7         | An array of bytes. The size of the array may reach the maximum length of JVM limitation (not less than 2147483639).       |
| `TAG_String`     | 8         | A UTF-8 string. The number of bytes corresponding to the string should not exceed 65535.                                  |
| `TAG_List`       | 9         | A list of tags of the same type. The size may also reach the maximum length of JVM limitation (not less than 2147483639). |
| `TAG_Compound`   | 10        | A collection of named tags, similar to associative arrays (also called maps or dictionaries).                             |
| `TAG_Int_Array`  | 11        | An array of 32-bit integers. The length may also reach the maximum length of JVM limitation (not less than 2147483639).   |
| `TAG_Long_Array` | 12        | An array of 64-bit integers. The length may also reach the maximum length of JVM limitation (not less than 2147483639).   |

We also refer to `TAG_Byte`, `TAG_Short`, `TAG_Int`, `TAG_Long`, `TAG_Float`, `TAG_Double`, and `TAG_String` as "primitive types".

## String format

The string format of NBT (also called SNBT) usually appears in texts related to game commands. For example:

```mcfunction
# summon an enchanted book of mending, dropped as an entity
/summon minecraft:item ~ ~ ~ {Item:{id:"minecraft:enchanted_book",Count:1b,tag:{StoredEnchantments:[{lvl:1s,id:"minecraft:mending"}]}}}
```

Here is the format of primitive types:

| Tag Type         | String Representation Example                                                           |
| ---------------- | --------------------------------------------------------------------------------------- |
| `TAG_Byte`       | `42b` (standard form) or `42B`.                                                         |
| `TAG_Short`      | `42s` (standard form) or `42S`.                                                         |
| `TAG_Int`        | `42` (no letter suffix).                                                                |
| `TAG_Long`       | `42l` (standard form) or `42L`.                                                         |
| `TAG_Float`      | `42.0f` (standard form), `42.0F`, or `42f`.                                             |
| `TAG_Double`     | `42.0d` (standard form), `42.0D`, `42d` or just `42.0`.                                 |
| `TAG_String`     | `"42"` (double quoted, standard form) or `'42'` (single quoted).                        |

Here is the format of number arrays:

| Tag Type         | String Representation Example                                                           |
| ---------------- | --------------------------------------------------------------------------------------- |
| `TAG_Byte_Array` | `[B;1,1,4,5,1,4]` (`B` should be upper case, and suffixes of numbers are not required). |
| `TAG_Int_Array`  | `[I;11,45,14]` (`I` should be upper case, and suffixes of numbers are not required).    |
| `TAG_Long_Array` | `[L;114,514]` (`L` should be upper case, and suffixes of numbers are not required).     |

`[]` is used for wrapping tag values as list tag children:

| Tag Type         | String Representation Example                                                           |
| ---------------- | --------------------------------------------------------------------------------------- |
| `TAG_List`       | `[{lvl:1s,id:"minecraft:mending"},{lvl:3s,id:"minecraft:fortune"}]`                     |

`{}` is user for wrapping named tags as compound tag children. Named tags use `:` to separate string names and tag values:

| Tag Type         | String Representation Example                                                           |
| ---------------- | --------------------------------------------------------------------------------------- |
| `TAG_Compound`   | `{id:"minecraft:stick",Count:1b}`                                                       |

End tags do not have string representations (while common implementations often use an empty string instead).

## Binary Format

The binary format is the actual format that Minecraft uses for loading and saving game data.

The basic components of binary data are called named tags, each of which consists of three parts:

> <code><span style="color:#f200ff">[Tag Type Number Id (BYTE)]</span> <span style="color:#0080e9">[Tag Name (UTF-8 STRING)]</span> <span style="color:#0bd300">[Tag Data]</span></code>

A UTF-8 string starts with 2 bytes representing the length, followed by its UTF-8 bytes:

> <code><span style="color:#e96900">[String Length (SHORT)]</span> <span style="color:#00de9b">[String Data]</span></code>

Here are some example names tags of primitive types:

| String Representation   | Binary Representation                                                          |
| ----------------------- | ------------------------------------------------------------------------------ |
| `value:42b`             | <code><span style="color:#f200ff">01</span> <span style="color:#e96900">00 05</span> <span style="color:#0080e9">76 61 6c 75 65</span> <span style="color:#0bd300">2a</span></code> |
| `value:42s`             | <code><span style="color:#f200ff">02</span> <span style="color:#e96900">00 05</span> <span style="color:#0080e9">76 61 6c 75 65</span> <span style="color:#0bd300">00 2a</span></code> |
| `value:42`              | <code><span style="color:#f200ff">03</span> <span style="color:#e96900">00 05</span> <span style="color:#0080e9">76 61 6c 75 65</span> <span style="color:#0bd300">00 00 00 2a</span></code> |
| `value:42l`             | <code><span style="color:#f200ff">04</span> <span style="color:#e96900">00 05</span> <span style="color:#0080e9">76 61 6c 75 65</span> <span style="color:#0bd300">00 00 00 00 00 00 00 2a</span></code> |
| `value:42.0f`           | <code><span style="color:#f200ff">05</span> <span style="color:#e96900">00 05</span> <span style="color:#0080e9">76 61 6c 75 65</span> <span style="color:#0bd300">42 28 00 00</span></code> |
| `value:42.0d`           | <code><span style="color:#f200ff">06</span> <span style="color:#e96900">00 05</span> <span style="color:#0080e9">76 61 6c 75 65</span> <span style="color:#0bd300">40 45 00 00 00 00 00 00</span></code> |
| `value:"42"`            | <code><span style="color:#f200ff">08</span> <span style="color:#e96900">00 05</span> <span style="color:#0080e9">76 61 6c 75 65</span> <span style="color:#e96900">00 02</span> <span style="color:#0bd300">34 32</span></code> |

A number array starts with 4 bytes representing the size, followed by its elements sequencially:

> <code><span style="color:#e96900">[Array Size (INT)]</span> <span style="color:#0bd300">[1st Element Data]</span> <span style="color:#a9d300">[2nd Element Data]</span> <strong>...</strong></code>

Here are some example named tags of number arrays:

| String Representation   | Binary Representation                                                          |
| ----------------------- | ------------------------------------------------------------------------------ |
| `value:[B;1,1,4,5,1,4]` | <code><span style="color:#f200ff">07</span> <span style="color:#e96900">00 05</span> <span style="color:#0080e9">76 61 6c 75 65</span> <span style="color:#e96900">00 00 00 06</span> <span style="color:#0bd300">01</span> <span style="color:#a9d300">01</span> <span style="color:#0bd300">04</span> <span style="color:#a9d300">05</span> <span style="color:#0bd300">01</span> <span style="color:#a9d300">04</span></code> |
| `value:[I;11,45,14]`    | <code><span style="color:#f200ff">0b</span> <span style="color:#e96900">00 05</span> <span style="color:#0080e9">76 61 6c 75 65</span> <span style="color:#e96900">00 00 00 03</span> <span style="color:#0bd300">00 00 00 0b</span> <span style="color:#a9d300">00 00 00 2d</span> <span style="color:#0bd300">00 00 00 0e</span></code> |
| `value:[L;114,514]`     | <code><span style="color:#f200ff">0c</span> <span style="color:#e96900">00 05</span> <span style="color:#0080e9">76 61 6c 75 65</span> <span style="color:#e96900">00 00 00 02</span> <span style="color:#0bd300">00 00 00 00 00 00 00 72</span> <span style="color:#a9d300">00 00 00 00 00 00 02 02</span></code> |

A compound tag firstly consists of multiple named tags sequentially, and finally ends with a byte of zero (number id of end tag):

> <code><span style="color:#f200ff">[1st Number Id (BYTE)]</span> <span style="color:#0080e9">[1st Name (UTF-8 STRING)]</span> <span style="color:#0bd300">[1st Data]</span> <span style="color:#f200ff">[2nd Number Id (BYTE)]</span> <strong>...</strong> <span style="color:#f200ff">[Zero (BYTE)]</span></code>

Here is an example compound tag:

| String Representation   | Binary Representation                                                          |
| ----------------------- | ------------------------------------------------------------------------------ |
| `value:{`<br>`  id:"minecraft:stick",`<br>`  Count:1b`<br>`}` | <code><span style="color:#f200ff">0a</span> <span style="color:#e96900">00 05</span> <span style="color:#0080e9">76 61 6c 75 65</span></code><br><code><span style="color:#f200ff">08</span> <span style="color:#e96900">00 02</span> <span style="color:#0080e9">69 64</span> <span style="color:#e96900">00 0f</span> <span style="color:#0bd300">6d 69 6e 65 63 72 61 66 74 3a 73 74 69 63 6b</span></code><br><code><span style="color:#f200ff">01</span> <span style="color:#e96900">00 05</span> <span style="color:#0080e9">43 6f 75 6e 74</span> <span style="color:#0bd300">01</span></code><br><code><span style="color:#f200ff">00</span></code> |

A list tag starts with a number id representing tag type of children, then 4 bytes representing the size, followed by its children sequencially:

> <code><span style="color:#e96900">[List Size (INT)]</span> <span style="color:#f200ff">[Child Number Id (BYTE)]</span> <span style="color:#0bd300">[1st Child Data]</span> <span style="color:#a9d300">[2nd Child Data]</span> <strong>...</strong></code>

Here is an example list tag:

| String Representation   | Binary Representation                                                          |
| ----------------------- | ------------------------------------------------------------------------------ |
| `value:[{`<br>`  lvl:1s,`<br>`  id:"minecraft:mending"`<br>`},{`<br>`  lvl:3s,`<br>`  id:"minecraft:fortune"`<br>`}]` | <code><span style="color:#f200ff">0a</span> <span style="color:#e96900">00 05</span> <span style="color:#0080e9">76 61 6c 75 65</span> <span style="color:#f200ff">0a</span> <span style="color:#e96900">00 00 00 02</span></code><br><code><span style="color:#f200ff">02</span> <span style="color:#e96900">00 03</span> <span style="color:#0080e9">6c 76 6c</span> <span style="color:#0bd300">00 01</span></code><br><code><span style="color:#f200ff">08</span> <span style="color:#e96900">00 02</span> <span style="color:#0080e9">69 64</span> <span style="color:#e96900">00 11</span> <span style="color:#0bd300">6d 69 6e 65 63 72 61 66 74 3a 6d 65 6e 64 69 6e 67</span></code><br><code><span style="color:#f200ff">00</span></code><br><code><span style="color:#f200ff">02</span> <span style="color:#e96900">00 03</span> <span style="color:#0080e9">6c 76 6c</span> <span style="color:#a9d300">00 03</span></code><br><code><span style="color:#f200ff">08</span> <span style="color:#e96900">00 02</span> <span style="color:#0080e9">69 64</span> <span style="color:#e96900">00 11</span> <span style="color:#a9d300">6d 69 6e 65 63 72 61 66 74 3a 66 6f 72 74 75 6e 65</span></code><br><code><span style="color:#f200ff">00</span></code> |

All the numbers mentioned above are stored as big-endian numbers (store `1s` as `00 01` but not `01 00`).

A complete NBT binary data always contains a single named tag. In most cases, the name of the named tag is empty (`""`).

## Compressed binary format

In most cases, Minecraft stores game data in GZIP compressed binary files instead of the uncompressed raw data shown above. However, some data is still saved without compression (for example, `servers.dat`).
