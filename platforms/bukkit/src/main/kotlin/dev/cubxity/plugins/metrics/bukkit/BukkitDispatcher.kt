/*
 *     This file is part of UnifiedMetrics.
 *
 *     UnifiedMetrics is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     UnifiedMetrics is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with UnifiedMetrics.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.cubxity.plugins.metrics.bukkit

import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import kotlin.coroutines.CoroutineContext

val isFolia: Boolean = runCatching {
    Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
}.isSuccess

@OptIn(InternalCoroutinesApi::class)
class BukkitDispatcher(private val plugin: JavaPlugin) : CoroutineDispatcher(), Delay {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        val ticks = timeMillis / 50
        if (isFolia) {
            val task = plugin.server.globalRegionScheduler.runDelayed(
                plugin,
                { continuation.apply { resumeUndispatched(Unit) } },
                ticks.coerceAtLeast(1)
            )
            continuation.invokeOnCancellation { task?.cancel() }
        } else {
            val task = plugin.server.scheduler.runTaskLater(
                plugin,
                Runnable { continuation.apply { resumeUndispatched(Unit) } },
                ticks
            )
            continuation.invokeOnCancellation { task.cancel() }
        }
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!context.isActive) return

        if (isFolia) {
            plugin.server.globalRegionScheduler.run(plugin) { block.run() }
        } else {
            if (Bukkit.isPrimaryThread()) {
                block.run()
            } else {
                plugin.server.scheduler.runTask(plugin, block)
            }
        }
    }
}
