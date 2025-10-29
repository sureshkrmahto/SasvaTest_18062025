package com.acme.javamigrator.engine.migration;

import com.acme.javamigrator.engine.util.FileUtils;

import java.io.IOException;
import java.nio.file.Path;

public class BackupService {

    public Path backupProject(Path sourceDir, Path backupZip) throws IOException {
        FileUtils.ensureDirectory(backupZip.getParent());
        return FileUtils.zipDirectory(sourceDir, backupZip);
    }
}
