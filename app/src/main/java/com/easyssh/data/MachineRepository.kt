package com.easyssh.data

import com.easyssh.domain.IpMode
import com.easyssh.domain.MachineDraft
import com.easyssh.domain.MachineProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MachineRepository(
    private val dao: MachineDao
) {
    val profiles: Flow<List<MachineProfile>> =
        dao.observeProfiles().map { entities -> entities.map(MachineEntity::toDomain) }

    suspend fun getProfile(id: Long): MachineProfile? =
        dao.getProfile(id)?.toDomain()

    suspend fun saveDraft(
        draft: MachineDraft,
        encryptedKeyFileName: String,
        keyDisplayName: String
    ): Long {
        val now = System.currentTimeMillis()
        val host = draft.host.trim().takeIf { draft.ipMode == IpMode.STATIC }
        val current = if (draft.id == 0L) null else dao.getProfile(draft.id)
        val preservedFingerprint = current
            ?.takeIf { it.host == host && it.port == draft.port.toInt() }
            ?.hostKeyFingerprint
        val entity = MachineEntity(
            id = draft.id,
            alias = draft.alias.trim(),
            username = draft.username.trim(),
            host = host,
            port = draft.port.toInt(),
            ipMode = draft.ipMode.name,
            encryptedKeyFileName = encryptedKeyFileName,
            keyDisplayName = keyDisplayName,
            hostKeyFingerprint = preservedFingerprint,
            createdAt = current?.createdAt ?: now,
            updatedAt = now
        )

        return if (draft.id == 0L) {
            dao.insert(entity)
        } else {
            dao.update(entity)
            draft.id
        }
    }

    suspend fun updateHostKeyFingerprint(id: Long, fingerprint: String) {
        dao.updateHostKeyFingerprint(
            id = id,
            fingerprint = fingerprint,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun delete(profile: MachineProfile) {
        dao.delete(profile.toEntity())
    }
}
