package ru.ai.assistant.application.metainfo

import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.ai.assistant.domain.metainfo.DialogMetaInfoEntity
import ru.ai.assistant.domain.metainfo.DialogMetaInfoRepository

@Service
class DialogMetaInfoEntityService(
    private val dialogMetaInfoRepository: DialogMetaInfoRepository
) {

    @Transactional
    suspend fun getOrCreateDialog(userId: Long): DialogMetaInfoEntity =
        dialogMetaInfoRepository.findAllByUserIdAndIsActiveTrue(userId).toList().firstOrNull().let { activeDialog ->
            activeDialog ?: dialogMetaInfoRepository.save(DialogMetaInfoEntity.create(userId))
        }
}