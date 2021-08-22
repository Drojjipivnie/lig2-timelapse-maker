package io.drojj.dao

import io.agroal.api.AgroalDataSource
import io.drojj.job.JobType
import java.io.File
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class VideosDAOImpl : VideosDAO {

    @Inject
    lateinit var dataSource: AgroalDataSource

    override fun saveVideoInformation(file: File, jobType: JobType) {
        dataSource.connection.use {
            val prepareStatement =
                it.prepareStatement("INSERT INTO lig2.videos (name, type, file_path) VALUES (?, ?, ?)")
            prepareStatement.setString(1, file.name)
            prepareStatement.setString(2, jobType.name)
            prepareStatement.setString(3, file.absolutePath)
            prepareStatement.executeUpdate()
        }
    }

}