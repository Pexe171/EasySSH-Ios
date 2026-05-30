package com.easyssh.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.easyssh.domain.IpMode
import com.easyssh.domain.MachineProfile

@Entity(tableName = "machine_profiles")
data class MachineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val alias: String,
    val username: String,
    val host: String?,
    val port: Int,
    val ipMode: String,
    val encryptedKeyFileName: String,
    val keyDisplayName: String,
    val hostKeyFingerprint: String?,
    val createdAt: Long,
    val updatedAt: Long
)

fun MachineEntity.toDomain(): MachineProfile = MachineProfile(
    id = id,
    alias = alias,
    username = username,
    host = host,
    port = port,
    ipMode = IpMode.valueOf(ipMode),
    encryptedKeyFileName = encryptedKeyFileName,
    keyDisplayName = keyDisplayName,
    hostKeyFingerprint = hostKeyFingerprint,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun MachineProfile.toEntity(): MachineEntity = MachineEntity(
    id = id,
    alias = alias,
    username = username,
    host = host,
    port = port,
    ipMode = ipMode.name,
    encryptedKeyFileName = encryptedKeyFileName,
    keyDisplayName = keyDisplayName,
    hostKeyFingerprint = hostKeyFingerprint,
    createdAt = createdAt,
    updatedAt = updatedAt
)

