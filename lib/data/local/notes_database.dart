import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';
import '../models/note_entity.dart';

class NotesDatabase {
  static final NotesDatabase instance = NotesDatabase._init();
  static Database? _database;

  NotesDatabase._init();

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDB('notes.db');
    return _database!;
  }

  Future<Database> _initDB(String filePath) async {
    final dbFolder = await getApplicationDocumentsDirectory();
    final path = join(dbFolder.path, filePath);

    return await openDatabase(
      path,
      version: 1,
      onCreate: _createDB,
    );
  }

  Future<void> _createDB(Database db, int version) async {
    await db.execute('''
      CREATE TABLE notes (
        id TEXT PRIMARY KEY,
        user_id TEXT,
        title TEXT NOT NULL,
        description TEXT NOT NULL,
        reminder_datetime INTEGER,
        priority_level INTEGER DEFAULT 1,
        is_completed INTEGER DEFAULT 0,
        created_at INTEGER NOT NULL,
        updated_at INTEGER NOT NULL,
        is_synced INTEGER DEFAULT 0,
        is_deleted INTEGER DEFAULT 0
      )
    ''');

    await db.execute('''
      CREATE INDEX idx_notes_user_id ON notes(user_id)
    ''');

    await db.execute('''
      CREATE INDEX idx_notes_reminder ON notes(reminder_datetime) WHERE reminder_datetime IS NOT NULL
    ''');
  }

  Future<void> insert(NoteEntity note) async {
    final db = await instance.database;
    await db.insert('notes', note.toMap(), conflictAlgorithm: ConflictAlgorithm.replace);
  }

  Future<void> update(NoteEntity note) async {
    final db = await instance.database;
    await db.update(
      'notes',
      note.toMap(),
      where: 'id = ?',
      whereArgs: [note.id],
    );
  }

  Future<void> delete(String id) async {
    final db = await instance.database;
    await db.delete(
      'notes',
      where: 'id = ?',
      whereArgs: [id],
    );
  }

  Future<List<NoteEntity>> getAll() async {
    final db = await instance.database;
    final result = await db.query('notes', orderBy: 'updated_at DESC');
    return result.map((json) => NoteEntity.fromMap(json)).toList();
  }

  Future<NoteEntity?> getById(String id) async {
    final db = await instance.database;
    final result = await db.query(
      'notes',
      where: 'id = ?',
      whereArgs: [id],
      limit: 1,
    );
    if (result.isEmpty) return null;
    return NoteEntity.fromMap(result.first);
  }

  Future<List<NoteEntity>> getUnsynced() async {
    final db = await instance.database;
    final result = await db.query(
      'notes',
      where: 'is_synced = ?',
      whereArgs: [0],
    );
    return result.map((json) => NoteEntity.fromMap(json)).toList();
  }

  Future<void> close() async {
    final db = await instance.database;
    await db.close();
  }
}