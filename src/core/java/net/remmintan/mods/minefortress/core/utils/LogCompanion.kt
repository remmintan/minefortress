package net.remmintan.mods.minefortress.core.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

open class LogCompanion(clazz: KClass<*>) {

    protected val log: Logger = LoggerFactory.getLogger(clazz.java)

}