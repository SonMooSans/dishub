package utils

import models.tables.records.GuildRecord
import models.tables.records.RequestRecord
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.interactions.Interaction
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.managers.channel.attribute.IPermissionContainerManager
import variables.MISSING_PERMISSIONS
import variables.NO_GUILD
import variables.NO_PERMISSIONS

val Guild.everyone: Long
    get() = this.publicRole.idLong

fun<M: IPermissionContainerManager<*, M>> M.allowUser(user: Long?, allow: Long, denyAll: Boolean = true): M {
    if (user == null) return this

    return putMemberPermissionOverride(user, allow,
        if (denyAll) Permission.ALL_PERMISSIONS else NO_PERMISSIONS
    )
}

fun<M: IPermissionContainerManager<*, M>> M.allowRole(role: Long?, allow: Long, denyAll: Boolean = true): M {
    if (role == null) return this

    return putRolePermissionOverride(role, allow,
        if (denyAll) Permission.ALL_PERMISSIONS else NO_PERMISSIONS
    )
}

fun RequestRecord.canEditRequest(user: Member): Boolean {
    return this.owner == user.idLong
}

fun GuildRecord.canModifyTags(guild: Guild, user: Member): Boolean {
    if (user.isOwner || guild.publicRole.idLong == managerRole)
        return true

    return user.isManager(managerRole)
}


fun GuildRecord.canModifyState(guild: Guild, user: Member): Boolean {
    if (user.isOwner || guild.publicRole.idLong == managerRole)
        return true

    return user.isManager(managerRole)
}

fun GuildRecord.canDeleteRequest(user: Member): Boolean {
    if (user.isOwner)
        return true

    return user.isManager(managerRole)
}

fun Member.isManager(manager: Long?): Boolean {
    return manager != null && roles.any { role ->
        role.idLong == manager
    }
}

/**
 * Calls onError function when:
 *
 * 1. Guild is null
 * 2. Permissions are missing
 */
inline fun IReplyCallback.verify(onError: () -> Unit) {

    if (guild == null) {
        this.error(NO_GUILD)
        return onError()
    }

    if (missingPermission()) {
        this.error(MISSING_PERMISSIONS)
        return onError()
    }
}

fun Interaction.missingPermission(): Boolean {
    return !guild!!.getMemberById(jda.selfUser.id)!!.hasPermission(Permission.ADMINISTRATOR)
}