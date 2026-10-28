  package snapshots
//        ^^^^^^^^^ reference scip-java maven . . snapshots/
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Container#
//      ⌄ enclosing_range_start scip-java maven . . snapshots/Container#`<init>`().
//                ⌄ enclosing_range_start scip-java maven . . snapshots/Container#[T]
//                                   ⌄ enclosing_range_start scip-java maven . . snapshots/Container#`<init>`().(items)
//                                   ⌄ enclosing_range_start scip-java maven . . snapshots/Container#items.
//                                   ⌄ enclosing_range_start scip-java maven . . snapshots/Container#items.get().
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
//                                               ^^^^^ definition scip-java maven . . snapshots/Container#items.get().
//                                                     kind Method
//                                                     display_name items
//                                                     signature_documentation
//                                                     > private get(): MutableList<T>
//                                                      ^^^^^^^^^^^ reference scip-java maven . . kotlin/collections/MutableList#
//                                                                  ^ reference scip-java maven . . snapshots/Container#[T]
//                                ⌃ enclosing_range_end scip-java maven . . snapshots/Container#[T]
//                                                                   ⌃ enclosing_range_end scip-java maven . . snapshots/Container#`<init>`().(items)
//                                                                   ⌃ enclosing_range_end scip-java maven . . snapshots/Container#items.
//                                                                   ⌃ enclosing_range_end scip-java maven . . snapshots/Container#items.get().
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
//        ^^^^^ reference scip-java maven . . snapshots/Container#items.get().
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
//                                                            ^^^^^ reference scip-java maven . . snapshots/Container#items.get().
//                                                                  ^^^^^^^^^^ reference scip-java maven . . kotlin/collections/Iterable#mapNotNull().
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
//                                                            ^^^^^^^^^^^ reference scip-java maven . . kotlin/collections/List#firstOrNull().
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
  
//⌄ enclosing_range_start scip-java maven . . snapshots/MyAlias#
  typealias MyAlias = Int
//          ^^^^^^^ definition scip-java maven . . snapshots/MyAlias#
//                  kind TypeAlias
//                  display_name MyAlias
//                  signature_documentation
//                  > public final typealias MyAlias = Int
//                  > 
//                    ^^^ reference scip-java maven . . kotlin/Int#
//                      ⌃ enclosing_range_end scip-java maven . . snapshots/MyAlias#
//⌄ enclosing_range_start scip-java maven . . snapshots/aliasValue.
//⌄ enclosing_range_start scip-java maven . . snapshots/aliasValue.get().
  val aliasValue: MyAlias = 42
//    ^^^^^^^^^^ definition scip-java maven . . snapshots/aliasValue.
//               kind Property
//               display_name aliasValue
//               signature_documentation
//               > public final val aliasValue: MyAlias
//    ^^^^^^^^^^ definition scip-java maven . . snapshots/aliasValue.get().
//               kind Method
//               display_name aliasValue
//               signature_documentation
//               > public get(): MyAlias
//                ^^^^^^^ reference scip-java maven . . kotlin/Int#
//                           ⌃ enclosing_range_end scip-java maven . . snapshots/aliasValue.
//                           ⌃ enclosing_range_end scip-java maven . . snapshots/aliasValue.get().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/identity().
//     ⌄ enclosing_range_start scip-java maven . . snapshots/identity().[T]
//                 ⌄ enclosing_range_start scip-java maven . . snapshots/identity().(x)
  fun <T> identity(x: T): T = x
//     ^ definition scip-java maven . . snapshots/identity().[T]
//       kind TypeParameter
//       display_name T
//       signature_documentation
//       > T
//        ^^^^^^^^ definition scip-java maven . . snapshots/identity().
//                 kind Method
//                 display_name identity
//                 signature_documentation
//                 > public final fun <T> identity(x: T): T
//                 ^ definition scip-java maven . . snapshots/identity().(x)
//                   kind Parameter
//                   display_name x
//                   signature_documentation
//                   > x: T
//                    ^ reference scip-java maven . . snapshots/identity().[T]
//                        ^ reference scip-java maven . . snapshots/identity().[T]
//                            ^ reference scip-java maven . . snapshots/identity().(x)
//     ⌃ enclosing_range_end scip-java maven . . snapshots/identity().[T]
//                    ⌃ enclosing_range_end scip-java maven . . snapshots/identity().(x)
//                            ⌃ enclosing_range_end scip-java maven . . snapshots/identity().
  
//⌄ enclosing_range_start scip-java maven . . snapshots/Box#
//      ⌄ enclosing_range_start scip-java maven . . snapshots/Box#`<init>`().
//          ⌄ enclosing_range_start scip-java maven . . snapshots/Box#[T]
//             ⌄ enclosing_range_start scip-java maven . . snapshots/Box#`<init>`().(value)
//             ⌄ enclosing_range_start scip-java maven . . snapshots/Box#value.
//             ⌄ enclosing_range_start scip-java maven . . snapshots/Box#value.get().
  class Box<T>(val value: T) {
//      ^^^ definition scip-java maven . . snapshots/Box#
//          kind Class
//          display_name Box
//          signature_documentation
//          > public final class Box<T> : Any
//      ^^^ definition scip-java maven . . snapshots/Box#`<init>`().
//          kind Constructor
//          display_name Box
//          signature_documentation
//          > public constructor<T>(value: T): Box<T>
//          ^ definition scip-java maven . . snapshots/Box#[T]
//            kind TypeParameter
//            display_name T
//            signature_documentation
//            > T
//                 ^^^^^ definition scip-java maven . . snapshots/Box#`<init>`().(value)
//                       kind Parameter
//                       display_name value
//                       signature_documentation
//                       > value: T
//                 ^^^^^ definition scip-java maven . . snapshots/Box#value.
//                       kind Property
//                       display_name value
//                       signature_documentation
//                       > public final val value: T
//                 ^^^^^ reference scip-java maven . . snapshots/Box#`<init>`().(value)
//                 ^^^^^ definition scip-java maven . . snapshots/Box#value.get().
//                       kind Method
//                       display_name value
//                       signature_documentation
//                       > public get(): T
//                        ^ reference scip-java maven . . snapshots/Box#[T]
//          ⌃ enclosing_range_end scip-java maven . . snapshots/Box#[T]
//                        ⌃ enclosing_range_end scip-java maven . . snapshots/Box#`<init>`().(value)
//                        ⌃ enclosing_range_end scip-java maven . . snapshots/Box#value.
//                        ⌃ enclosing_range_end scip-java maven . . snapshots/Box#value.get().
//                         ⌃ enclosing_range_end scip-java maven . . snapshots/Box#`<init>`().
//    ⌄ enclosing_range_start scip-java maven . . snapshots/Box#unwrap().
      fun unwrap(): T = value
//        ^^^^^^ definition scip-java maven . . snapshots/Box#unwrap().
//               kind Method
//               display_name unwrap
//               signature_documentation
//               > public final fun unwrap(): T
//                  ^ reference scip-java maven . . snapshots/Box#[T]
//                      ^^^^^ reference scip-java maven . . snapshots/Box#value.
//                      ^^^^^ reference scip-java maven . . snapshots/Box#value.get().
//                          ⌃ enclosing_range_end scip-java maven . . snapshots/Box#unwrap().
  }
//⌃ enclosing_range_end scip-java maven . . snapshots/Box#
  
