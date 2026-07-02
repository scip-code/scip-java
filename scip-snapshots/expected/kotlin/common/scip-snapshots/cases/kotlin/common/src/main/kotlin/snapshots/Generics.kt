  package snapshots
//        ^^^^^^^^^ reference scip-java maven . . snapshots/
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Container#
//                ⌄ enclosing_range_start scip-java maven . . snapshots/Container#[T]
//                                  ⌄ enclosing_range_start scip-java maven . . snapshots/Container#`<init>`().
//                                   ⌄ enclosing_range_start scip-java maven . . snapshots/Container#`<init>`().(items)
//                                   ⌄ enclosing_range_start scip-java maven . . snapshots/Container#items.
//                                   ⌄ enclosing_range_start scip-java maven . . snapshots/Container#getItems().
  class Container<T : Comparable<T>>(private val items: MutableList<T>) {
//      ^^^^^^^^^ definition scip-java maven . . snapshots/Container#
//                display_name Container
//                signature_documentation
//                > public final class Container<T : Comparable<T>> : Any
//      ^^^^^^^^^ definition scip-java maven . . snapshots/Container#`<init>`().
//                display_name Container
//                signature_documentation
//                > public constructor<T : Comparable<T>>(items: MutableList<T>): Container<T>
//                ^ definition scip-java maven . . snapshots/Container#[T]
//                  display_name FirTypeParameterSymbol T
//                  signature_documentation
//                  > T : Comparable<T>
//                                               ^^^^^ definition scip-java maven . . snapshots/Container#`<init>`().(items)
//                                                     display_name items
//                                                     signature_documentation
//                                                     > items: MutableList<T>
//                                               ^^^^^ definition scip-java maven . . snapshots/Container#items.
//                                                     display_name items
//                                                     signature_documentation
//                                                     > private final val items: MutableList<T>
//                                               ^^^^^ reference scip-java maven . . snapshots/Container#`<init>`().(items)
//                                               ^^^^^ definition scip-java maven . . snapshots/Container#getItems().
//                                                     display_name items
//                                                     signature_documentation
//                                                     > private get(): MutableList<T>
//                                                      ^^^^^^^^^^^^^^ reference scip-java maven . . kotlin/collections/MutableList#
//                                ⌃ enclosing_range_end scip-java maven . . snapshots/Container#[T]
//                                                                   ⌃ enclosing_range_end scip-java maven . . snapshots/Container#`<init>`().(items)
//                                                                   ⌃ enclosing_range_end scip-java maven . . snapshots/Container#items.
//                                                                   ⌃ enclosing_range_end scip-java maven . . snapshots/Container#getItems().
//                                                                    ⌃ enclosing_range_end scip-java maven . . snapshots/Container#`<init>`().
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Container#add().
//            ⌄ enclosing_range_start scip-java maven . . snapshots/Container#add().(item)
      fun add(item: T): Container<T> {
//        ^^^ definition scip-java maven . . snapshots/Container#add().
//            display_name add
//            signature_documentation
//            > public final fun add(item: T): Container<T>
//            ^^^^ definition scip-java maven . . snapshots/Container#add().(item)
//                 display_name item
//                 signature_documentation
//                 > item: T
//                      ^^^^^^^^^^^^ reference scip-java maven . . snapshots/Container#
//                  ⌃ enclosing_range_end scip-java maven . . snapshots/Container#add().(item)
          items.add(item)
//        ^^^^^ reference scip-java maven . . snapshots/Container#items.
//        ^^^^^ reference scip-java maven . . snapshots/Container#getItems().
//              ^^^ reference scip-java maven . . kotlin/collections/MutableList#add().
//                  ^^^^ reference scip-java maven . . snapshots/Container#add().(item)
          return this
      }
//    ⌃ enclosing_range_end scip-java maven . . snapshots/Container#add().
  
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Container#mapItems().
//         ⌄ enclosing_range_start scip-java maven . . snapshots/Container#mapItems().[R]
//                           ⌄ enclosing_range_start scip-java maven . . snapshots/Container#mapItems().(transform)
      fun <R : Any> mapItems(transform: (T) -> R?): List<R> = items.mapNotNull(transform)
//         ^ definition scip-java maven . . snapshots/Container#mapItems().[R]
//           display_name FirTypeParameterSymbol R
//           signature_documentation
//           > R : Any
//                  ^^^^^^^^ definition scip-java maven . . snapshots/Container#mapItems().
//                           display_name mapItems
//                           signature_documentation
//                           > public final fun <R : Any> mapItems(transform: (T) -> R?): List<R>
//                           ^^^^^^^^^ definition scip-java maven . . snapshots/Container#mapItems().(transform)
//                                     display_name transform
//                                     signature_documentation
//                                     > transform: (T) -> R?
//                                      ^^^^^^^^^ reference scip-java maven . . kotlin/Function1#
//                                                  ^^^^^^^ reference scip-java maven . . kotlin/collections/List#
//                                                            ^^^^^ reference scip-java maven . . snapshots/Container#items.
//                                                            ^^^^^ reference scip-java maven . . snapshots/Container#getItems().
//                                                                  ^^^^^^^^^^ reference scip-java maven . . kotlin/collections/mapNotNull(+1).
//                                                                             ^^^^^^^^^ reference scip-java maven . . snapshots/Container#mapItems().(transform)
//               ⌃ enclosing_range_end scip-java maven . . snapshots/Container#mapItems().[R]
//                                              ⌃ enclosing_range_end scip-java maven . . snapshots/Container#mapItems().(transform)
//                                                                                      ⌃ enclosing_range_end scip-java maven . . snapshots/Container#mapItems().
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/Container#
  
//⌄ enclosing_range_start scip-java maven . . snapshots/firstOrSelf().
//     ⌄ enclosing_range_start scip-java maven . . snapshots/firstOrSelf().[T]
//                    ⌄ enclosing_range_start scip-java maven . . snapshots/firstOrSelf().(items)
//                                    ⌄ enclosing_range_start scip-java maven . . snapshots/firstOrSelf().(fallback)
  fun <T> firstOrSelf(items: List<T>, fallback: T): T = items.firstOrNull() ?: fallback
//     ^ definition scip-java maven . . snapshots/firstOrSelf().[T]
//       display_name FirTypeParameterSymbol T
//       signature_documentation
//       > T
//        ^^^^^^^^^^^ definition scip-java maven . . snapshots/firstOrSelf().
//                    display_name firstOrSelf
//                    signature_documentation
//                    > public final fun <T> firstOrSelf(items: List<T>, fallback: T): T
//                    ^^^^^ definition scip-java maven . . snapshots/firstOrSelf().(items)
//                          display_name items
//                          signature_documentation
//                          > items: List<T>
//                           ^^^^^^^ reference scip-java maven . . kotlin/collections/List#
//                                    ^^^^^^^^ definition scip-java maven . . snapshots/firstOrSelf().(fallback)
//                                             display_name fallback
//                                             signature_documentation
//                                             > fallback: T
//                                                      ^^^^^ reference scip-java maven . . snapshots/firstOrSelf().(items)
//                                                            ^^^^^^^^^^^ reference scip-java maven . . kotlin/collections/firstOrNull(+19).
//                                                                             ^^^^^^^^ reference scip-java maven . . snapshots/firstOrSelf().(fallback)
//     ⌃ enclosing_range_end scip-java maven . . snapshots/firstOrSelf().[T]
//                                 ⌃ enclosing_range_end scip-java maven . . snapshots/firstOrSelf().(items)
//                                              ⌃ enclosing_range_end scip-java maven . . snapshots/firstOrSelf().(fallback)
//                                                                                    ⌃ enclosing_range_end scip-java maven . . snapshots/firstOrSelf().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/StringContainer#
  typealias StringContainer = Container<String>
//          ^^^^^^^^^^^^^^^ definition scip-java maven . . snapshots/StringContainer#
//                          display_name FirTypeAliasSymbol snapshots/StringContainer
//                          signature_documentation
//                          > public final typealias StringContainer = Container<String>
//                          > 
//                                            ⌃ enclosing_range_end scip-java maven . . snapshots/StringContainer#
  
//⌄ enclosing_range_start scip-java maven . . snapshots/useContainer().
//                 ⌄ enclosing_range_start scip-java maven . . snapshots/useContainer().(container)
  fun useContainer(container: StringContainer): StringContainer = container.add("hello")
//    ^^^^^^^^^^^^ definition scip-java maven . . snapshots/useContainer().
//                 display_name useContainer
//                 signature_documentation
//                 > public final fun useContainer(container: {snapshots/StringContainer=} Container<String>): {snapshots/StringContainer=} Container<String>
//                 ^^^^^^^^^ definition scip-java maven . . snapshots/useContainer().(container)
//                           display_name container
//                           signature_documentation
//                           > container: {snapshots/StringContainer=} Container<String>
//                            ^^^^^^^^^^^^^^^ reference scip-java maven . . snapshots/Container#
//                                              ^^^^^^^^^^^^^^^ reference scip-java maven . . snapshots/Container#
//                                                                ^^^^^^^^^ reference scip-java maven . . snapshots/useContainer().(container)
//                                                                          ^^^ reference scip-java maven . . snapshots/Container#add().
//                                          ⌃ enclosing_range_end scip-java maven . . snapshots/useContainer().(container)
//                                                                                     ⌃ enclosing_range_end scip-java maven . . snapshots/useContainer().
  
