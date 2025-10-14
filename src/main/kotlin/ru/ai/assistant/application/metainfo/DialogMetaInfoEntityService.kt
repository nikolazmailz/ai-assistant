package ru.ai.assistant.application.metainfo

import kotlinx.coroutines.flow.toList
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.ai.assistant.domain.metainfo.DialogMetaInfoEntity
import ru.ai.assistant.domain.metainfo.DialogMetaInfoRepository
import java.util.UUID

@Service
class DialogMetaInfoEntityService(
    private val dialogMetaInfoRepository: DialogMetaInfoRepository
) {

    @Transactional
    suspend fun getOrCreateDialogMetaInfo(userId: Long): DialogMetaInfoEntity =
        dialogMetaInfoRepository.findAllByUserIdAndIsActiveTrue(userId).toList().firstOrNull().let { activeDialog ->
            activeDialog ?: dialogMetaInfoRepository.save(DialogMetaInfoEntity.create(userId))
        }


    suspend fun getDialogMetaInfoByUserId(userId: Long): DialogMetaInfoEntity =
        dialogMetaInfoRepository.findAllByUserIdAndIsActiveTrue(userId).toList().first()

    suspend fun getDialogMetaInfoById(id: UUID): DialogMetaInfoEntity =
        dialogMetaInfoRepository.findById(id) ?: throw RuntimeException("DialogMetaInfoEntity not found")
}