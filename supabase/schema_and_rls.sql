-- ==============================================================================
-- .notes (dotnotes) - Supabase Database Schema & Row Level Security (RLS)
-- ==============================================================================

-- 1. Create the `notes` table
CREATE TABLE IF NOT EXISTS public.notes (
    id TEXT PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL DEFAULT '',
    content TEXT NOT NULL DEFAULT '',
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    reminder_time BIGINT,
    priority INT NOT NULL DEFAULT 0,
    is_alarm_dismissed BOOLEAN NOT NULL DEFAULT FALSE,
    snooze_duration_min INT NOT NULL DEFAULT 5,
    repeat_interval TEXT NOT NULL DEFAULT 'NONE',
    color_theme TEXT NOT NULL DEFAULT 'DEFAULT',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 2. Create indices for performance
CREATE INDEX IF NOT EXISTS idx_notes_user_id ON public.notes(user_id);
CREATE INDEX IF NOT EXISTS idx_notes_updated_at ON public.notes(updated_at);

-- 3. Enable Row Level Security (RLS) - CRITICAL FOR USER PRIVACY
ALTER TABLE public.notes ENABLE ROW LEVEL SECURITY;

-- 4. Create Isolation Policies
-- Drop existing policies if any to prevent duplicate error
DROP POLICY IF EXISTS "Users can view their own notes" ON public.notes;
DROP POLICY IF EXISTS "Users can insert their own notes" ON public.notes;
DROP POLICY IF EXISTS "Users can update their own notes" ON public.notes;
DROP POLICY IF EXISTS "Users can delete their own notes" ON public.notes;

-- Users can only select their own notes
CREATE POLICY "Users can view their own notes"
ON public.notes
FOR SELECT
USING (auth.uid() = user_id);

-- Users can only insert notes with their own user_id
CREATE POLICY "Users can insert their own notes"
ON public.notes
FOR INSERT
WITH CHECK (auth.uid() = user_id);

-- Users can only update their own notes
CREATE POLICY "Users can update their own notes"
ON public.notes
FOR UPDATE
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- Users can only delete their own notes
CREATE POLICY "Users can delete their own notes"
ON public.notes
FOR DELETE
USING (auth.uid() = user_id);
