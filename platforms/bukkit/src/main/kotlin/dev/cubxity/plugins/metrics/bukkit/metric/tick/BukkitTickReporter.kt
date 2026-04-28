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

package dev.cubxity.plugins.metrics.bukkit.metric.tick

import dev.cubxity.plugins.metrics.bukkit.bootstrap.UnifiedMetricsBukkitBootstrap
import dev.cubxity.plugins.metrics.bukkit.isFolia
import io.papermc.paper.threadedregions.scheduler.ScheduledTask

class BukkitTickReporter(
    private val metric: TickCollection,
    private val bootstrap: UnifiedMetricsBukkitBootstrap
) : TickReporter, Runnable {
    private var taskId: Int? = null
    private var foliaTask: ScheduledTask? = null

    override fun initialize() {
        if (isFolia) {
            foliaTask = bootstrap.server.globalRegionScheduler.runAtFixedRate(bootstrap, { run() }, 1L, 1L)
        } else {
            taskId = bootstrap.server.scheduler.runTaskTimer(bootstrap, this, 1, 1).taskId
        }
    }

    override fun dispose() {
        foliaTask?.cancel()
        foliaTask = null
        taskId?.let { bootstrap.server.scheduler.cancelTask(it) }
        taskId = null
    }

    override fun run() {
        metric.onTick(0.0)
    }
}
