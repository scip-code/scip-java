  package snapshots
//        ^^^^^^^^^ reference scip-java maven . . snapshots/
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Container#
//      ⌄ enclosing_range_start scip-java maven . . snapshots/Container#`<init>`().
//                ⌄ enclosing_range_start scip-java maven . . snapshots/Container#[T]
//                                   ⌄ enclosing_range_start scip-java maven . . snapshots/Container#`<init>`().(items)
//                                   ⌄ enclosing_range_start scip-java maven . . snapshots/Container#items.
//                                   ⌄ enclosing_range_start scip-java maven . . snapshots/Container#getItems().
  class Container<T : Comparable<T>>(private val items: MutableList<T>) {
//      ^^^^^^^^^ definition scip-java maven . . snapshots/Container#
//                kind Class
//                display_name Container
//                signature_documentation
//                > public final class Container<T : Comparable<T>> : Any
//      ^^^^^^^^^ definition scip-java maven . . snapshots/Container#`<init>`().
//                kind Constructor
//                display_name Container
//                signature_documentation
//                > public constructor<T : Comparable<T>>(items: MutableList<T>): Container<T>
//                ^ definition scip-java maven . . snapshots/Container#[T]
//                  kind TypeParameter
//                  display_name T
//                  signature_documentation
//                  > T : Comparable<T>
//                    ^^^^^^^^^^ reference scip-java maven . . kotlin/Comparable#
//                               ^ reference scip-java maven . . snapshots/Container#[T]
//                                               ^^^^^ definition scip-java maven . . snapshots/Container#`<init>`().(items)
//                                                     kind Parameter
//                                                     display_name items
//                                                     signature_documentation
//                                                     > items: MutableList<T>
//                                               ^^^^^ definition scip-java maven . . snapshots/Container#items.
//                                                     kind Property
//                                                     display_name items
//                                                     signature_documentation
//                                                     > private final val items: MutableList<T>
//                                               ^^^^^ reference scip-java maven . . snapshots/Container#`<init>`().(items)
//                                               ^^^^^ definition scip-java maven . . snapshots/Container#getItems().
//                                                     kind Method
//                                                     display_name items
//                                                     signature_documentation
//                                                     > private get(): MutableList<T>
//                                                      ^^^^^^^^^^^ reference scip-java maven . . kotlin/collections/MutableList#
//                                                                  ^ reference scip-java maven . . snapshots/Container#[T]
//                                ⌃ enclosing_range_end scip-java maven . . snapshots/Container#[T]
//                                                                   ⌃ enclosing_range_end scip-java maven . . snapshots/Container#`<init>`().(items)
//                                                                   ⌃ enclosing_range_end scip-java maven . . snapshots/Container#items.
//                                                                   ⌃ enclosing_range_end scip-java maven . . snapshots/Container#getItems().
//                                                                    ⌃ enclosing_range_end scip-java maven . . snapshots/Container#`<init>`().
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Container#add().
//            ⌄ enclosing_range_start scip-java maven . . snapshots/Container#add().(item)
      fun add(item: T): Container<T> {
//        ^^^ definition scip-java maven . . snapshots/Container#add().
//            kind Method
//            display_name add
//            signature_documentation
//            > public final fun add(item: T): Container<T>
//            ^^^^ definition scip-java maven . . snapshots/Container#add().(item)
//                 kind Parameter
//                 display_name item
//                 signature_documentation
//                 > item: T
//                  ^ reference scip-java maven . . snapshots/Container#[T]
//                      ^^^^^^^^^ reference scip-java maven . . snapshots/Container#
//                                ^ reference scip-java maven . . snapshots/Container#[T]
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
//           kind TypeParameter
//           display_name R
//           signature_documentation
//           > R : Any
//             ^^^ reference scip-java maven . . kotlin/Any#
//                  ^^^^^^^^ definition scip-java maven . . snapshots/Container#mapItems().
//                           kind Method
//                           display_name mapItems
//                           signature_documentation
//                           > public final fun <R : Any> mapItems(transform: (T) -> R?): List<R>
//                           ^^^^^^^^^ definition scip-java maven . . snapshots/Container#mapItems().(transform)
//                                     kind Parameter
//                                     display_name transform
//                                     signature_documentation
//                                     > transform: (T) -> R?
//                                       ^ reference scip-java maven . . snapshots/Container#[T]
//                                             ^ reference scip-java maven . . snapshots/Container#mapItems().[R]
//                                                  ^^^^ reference scip-java maven . . kotlin/collections/List#
//                                                       ^ reference scip-java maven . . snapshots/Container#mapItems().[R]
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
//       kind TypeParameter
//       display_name T
//       signature_documentation
//       > T
//        ^^^^^^^^^^^ definition scip-java maven . . snapshots/firstOrSelf().
//                    kind Method
//                    display_name firstOrSelf
//                    signature_documentation
//                    > public final fun <T> firstOrSelf(items: List<T>, fallback: T): T
//                    ^^^^^ definition scip-java maven . . snapshots/firstOrSelf().(items)
//                          kind Parameter
//                          display_name items
//                          signature_documentation
//                          > items: List<T>
//                           ^^^^ reference scip-java maven . . kotlin/collections/List#
//                                ^ reference scip-java maven . . snapshots/firstOrSelf().[T]
//                                    ^^^^^^^^ definition scip-java maven . . snapshots/firstOrSelf().(fallback)
//                                             kind Parameter
//                                             display_name fallback
//                                             signature_documentation
//                                             > fallback: T
//                                              ^ reference scip-java maven . . snapshots/firstOrSelf().[T]
//                                                  ^ reference scip-java maven . . snapshots/firstOrSelf().[T]
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
//                          kind TypeAlias
//                          display_name StringContainer
//                          signature_documentation
//                          > public final typealias StringContainer = Container<String>
//                          > 
//                            ^^^^^^^^^ reference scip-java maven . . snapshots/Container#
//                                      ^^^^^^ reference scip-java maven . . kotlin/String#
//                                            ⌃ enclosing_range_end scip-java maven . . snapshots/StringContainer#
  
//⌄ enclosing_range_start scip-java maven . . snapshots/useContainer().
//                 ⌄ enclosing_range_start scip-java maven . . snapshots/useContainer().(container)
  fun useContainer(container: StringContainer): StringContainer = container.add("hello")
//    ^^^^^^^^^^^^ definition scip-java maven . . snapshots/useContainer().
//                 kind Method
//                 display_name useContainer
//                 signature_documentation
//                 > public final fun useContainer(container: StringContainer): StringContainer
//                 ^^^^^^^^^ definition scip-java maven . . snapshots/useContainer().(container)
//                           kind Parameter
//                           display_name container
//                           signature_documentation
//                           > container: StringContainer
//                            ^^^^^^^^^^^^^^^ reference scip-java maven . . snapshots/Container#
//                                              ^^^^^^^^^^^^^^^ reference scip-java maven . . snapshots/Container#
//                                                                ^^^^^^^^^ reference scip-java maven . . snapshots/useContainer().(container)
//                                                                          ^^^ reference scip-java maven . . snapshots/Container#add().
//                                          ⌃ enclosing_range_end scip-java maven . . snapshots/useContainer().(container)
//                                                                                     ⌃ enclosing_range_end scip-java maven . . snapshots/useContainer().
  
