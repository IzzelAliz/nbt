## NBT

Java NBT (Named Binary Tag) library.

## Getting started

For Gradle projects:

```groovy
repositories {
    maven { url 'https://maven.izzel.io/releases' }
}
dependencies {
    compile 'io.izzel:nbt:VERSION'
}
```

The library requires Java 8 or above.

## Code examples

Conversion between different formats:

```java
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

import io.izzel.nbt.util.*;

// string <===> gzipped binary
public void copyLevelData() throws IOException {
    Path input = Paths.get("level.dat");
    Path output = Paths.get("level.dat_old");

    String stringNbt = new CompressedNbtReader(input).toStringNbt();
    new StringNbtReader(stringNbt).toCompressedBinaryFile(output);
}

// string <===> uncompressed binary
public void backupServers() throws IOException {
    Path input = Paths.get("servers.dat");
    Path output = Paths.get("backup/servers.dat");

    String stringNbt = new NbtReader(input).toStringNbt();
    new StringNbtReader(stringNbt).toBinaryFile(output);
}
```

Dealing with in-memory objects:

```java
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

import io.izzel.nbt.*;
import io.izzel.nbt.util.*;

public void filterOutLocalServers() throws IOException {
    Path file = Paths.get("servers.dat");

    CompoundTag root = new NbtReader(file).toCompoundTag();
    ListTag serverList = root.getListOrDefault("servers");

    ListTag.Builder builder = ListTag.builder();
    for (int i = 0; i < serverList.size(); ++i) {
        CompoundTag server = serverList.getCompoundOrDefault(i);
        String ip = server.getString("ip", /*fallback*/"127.0.0.1");
        if (!"127.0.0.1".equals(ip) && !"localhost".equals(ip)) {
            builder.add(server);
        }
    }

    ListTag newServerList = builder.build();
    CompoundTag newRoot = CompoundTag.builder().add("servers", newServerList).build();

    new TagReader(newRoot).toBinaryFile(file);
}
```

## License

The library is under [MIT license](https://github.com/IzzelAliz/nbt/tree/master/LICENSE).

## Built from source

Linux or macOS:

```shell
git clone https://github.com/IzzelAliz/nbt.git IzzelAlizNBT --depth 20
cd IzzelAlizNBT/
./gradlew build --exclude-task test
```

Microsoft Windows:

```shell
git clone https://github.com/IzzelAliz/nbt.git IzzelAlizNBT --depth 20
cd IzzelAlizNBT/
gradlew.bat build --exclude-task test
```

Please make sure you have installed [Git](https://git-scm.com/) on your machine.
