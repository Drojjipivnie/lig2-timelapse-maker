package io.drojj.lig2.timelapse.maker.dao

import io.agroal.api.AgroalDataSource
import io.drojj.lig2.timelapse.maker.TimelapseType
import java.io.File
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class TimelapseRepositoryImpl : TimelapseRepository {

    @Inject
    lateinit var dataSource: AgroalDataSource

    override fun saveInformation(file: File, timelapseType: TimelapseType) {
        dataSource.connection.use { con ->
            con.prepareStatement("INSERT INTO lig2.videos (name, type, file_path) VALUES (?, ?, ?)").use { stmt ->
                stmt.setString(1, file.parentFile.name)
                stmt.setString(2, timelapseType.name)
                stmt.setString(3, file.absolutePath)
                stmt.executeUpdate()
            }
        }
    }
}